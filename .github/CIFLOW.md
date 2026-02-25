# Development Version and Branch Handling

## Branching Strategy

The JUDO NG modules follow a **GitFlow**-based branching and versioning policy ([see tutorial](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow)).

### Branch Types

| Branch | Pattern | Purpose |
|--------|---------|---------|
| **develop** | `develop` | Latest development sources for the active version |
| **feature** | `feature/JNG-NNN_short_summary` | New features, branched from `develop` |
| **release** | `release/x.y.z` or `x_y_betaN` | Stabilization branches for upcoming releases |
| **bugfix** | `bugfix/JNG-NNN_short_summary` | Fixes on release branches; must be applied to all newer versions |
| **support** | `support/JNG-NNN_short_summary` | Minor changes to previous releases; merged back to the release branch |
| **hotfix** | `hotfix/JNG-NNN_short_summary` | Urgent fixes applied to both release and `master` branches |
| **master** | `master` | Latest released sources |

### Branch Lifecycle

```mermaid
gitGraph
    commit id: "initial"
    branch develop
    commit id: "dev-1"
    branch feature/JNG-1
    commit id: "feat-1"
    commit id: "feat-2"
    checkout develop
    merge feature/JNG-1 id: "merge-feat"
    commit id: "dev-2"
    branch release/1.0
    commit id: "rc-1"
    branch bugfix/JNG-4
    commit id: "bugfix"
    checkout release/1.0
    merge bugfix/JNG-4 id: "merge-fix"
    checkout develop
    merge release/1.0 id: "merge-release"
    checkout main
    merge release/1.0 id: "release-1.0"
```

## Version Numbers

Versions follow **semantic versioning** with these rules:

| Scenario | Version Change |
|----------|---------------|
| Starting a **feature** branch | No change |
| Starting a **release** branch | 2nd number incremented on `develop` |
| Starting a **bugfix** branch | No change (applied to release branches before release) |
| Starting a **support** branch | 3rd number incremented |
| Starting a **hotfix** branch | 4th number incremented |

## GitHub Actions CI/CD Workflows

The project uses several interconnected GitHub Actions workflows. Issue tracking uses [JIRA](https://blackbelt.atlassian.net/jira/dashboards).

> **Important:** Every commit and pull request must reference a JIRA ticket number (`JNG-xxx`).

### build.yml — Main Build Pipeline

Triggered on pushes to `develop` and pull requests targeting `develop`, `master`, `increment/*`, or `release/*`.

```mermaid
flowchart TD
    trigger["Push to develop<br/>or PR to develop/master/increment/release"]
    trigger --> branch_check{Branch type?}
    branch_check -->|"master, release/*"| version_release["Version from pom.xml<br/>(without -SNAPSHOT)"]
    branch_check -->|"develop, increment/*"| version_dev["Version: major.minor.qual.date_commitId_branch"]
    version_release --> build["Build & deploy to Nexus"]
    version_dev --> build
    build --> tag["Create git tag v&lt;version&gt;"]
    tag --> is_release{increment/* or release/*?}
    is_release -->|Yes| merge_tag["Create merge-pr/&lt;version&gt; tag"]
    merge_tag --> trigger_merge["Triggers merge-pr-tagged.yml"]
    is_release -->|No| is_develop{develop?}
    is_develop -->|Yes| changelog["Build changelog"]
    changelog --> gh_release["Create GitHub pre-release"]
```

### merge-pr-tagged.yml — Auto-merge Handler

Triggered when a `merge-pr/*` tag is pushed.

```mermaid
flowchart TD
    trigger["Push merge-pr/* tag"]
    trigger --> parse["Extract version from tag"]
    parse --> check{Version format?}
    check -->|"major.minor.qualifier"| merge_master["Merge PR to master"]
    merge_master --> trigger_master["Triggers create-release-on-master.yml"]
    check -->|Other| squash_develop["Squash PR to develop"]
    squash_develop --> trigger_build["Triggers build.yml"]
    merge_master --> cleanup["Delete merge-pr tag"]
    squash_develop --> cleanup
```

### create-release-on-master.yml — Release Publisher

Triggered when code is pushed to `master`.

```mermaid
flowchart TD
    trigger["Push to master"] --> get_version["Get version from tag"]
    get_version --> changelog["Build changelog"]
    changelog --> release["Create GitHub release (latest)"]
```

### release.yml — Release Creator

Manually triggered with a version parameter (either `auto` or `major.minor.qualifier`).

```mermaid
flowchart TD
    trigger["Manual trigger with version"]
    trigger --> check{Version = 'auto'?}
    check -->|Yes| auto["Read version from pom.xml<br/>(strip -SNAPSHOT)"]
    check -->|No| manual["Use given version"]
    auto --> next["Calculate next version<br/>(qualifier + 1)"]
    manual --> next
    next --> pr_master["Create PR to master<br/>with release version"]
    next --> pr_develop["Create PR to develop<br/>with next version"]
    pr_master --> build1["Triggers build.yml"]
    pr_develop --> build2["Triggers build.yml"]
```

### Other Workflows

| Workflow | Purpose |
|----------|---------|
| `bump-version.yml` | Bumps version on release branches |
| `create-release-tagged.yml` | Creates releases from tags |
| `build-dependabot.yml` | Handles Dependabot PRs |
| `jira-description-to-pr.yml` | Copies JIRA ticket descriptions into PR bodies |
| `delete-old-draft-releases.yml` | Cleans up draft releases |
| `sync-labels.yml` | Synchronizes issue labels |

## Workflow Interaction Overview

```mermaid
flowchart LR
    subgraph Triggers
        push_dev["Push to develop"]
        pr["Pull Request"]
        push_master["Push to master"]
        manual["Manual release"]
        merge_tag["merge-pr/* tag"]
    end

    subgraph Workflows
        build["build.yml"]
        merge_pr["merge-pr-tagged.yml"]
        create_master["create-release-on-master.yml"]
        release["release.yml"]
    end

    push_dev --> build
    pr --> build
    build -->|"creates merge-pr tag"| merge_tag
    merge_tag --> merge_pr
    merge_pr -->|"merge to master"| push_master
    merge_pr -->|"squash to develop"| push_dev
    push_master --> create_master
    manual --> release
    release -->|"creates PRs"| pr
```
