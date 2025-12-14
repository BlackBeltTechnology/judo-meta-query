# Development version and branch handling

## Table of Contents

- [Branches](#branches)
- [Version numbers](#version-numbers)
- [How to develop](#how-to-develop)

## Branches

Versioning policy of JUDO NG modules are based on GitFlow: https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow.

Branches:

* **develop**: development branch contains latest development sources of the last active version
* **feature/JNG-NUMBER_short_summary**: feature branches are based on **develop** and contains sources of new features that will be included in last active version
* **(release/)1_0_beta1**: release branches of 1.0-beta1 (release/ prefix is still reserved for CI)
* **bugfix/JNG-NUMBER_short_summary**, **support/JNG-NUMBER_short_summary**: bugfix and support branches are based on release branches and must be applied to release and development branches of newer versions too
* **master**: contains latest released sources of the last active version

```mermaid
flowchart LR
    subgraph Branches
        master[master]
        develop[develop]
        feature1[feature/JNG-1]
        feature2[feature/JNG-2]
        feature3[feature/JNG-3]
        release1[release/1.0-beta1]
        bugfix[bugfix/JNG-4]
        release2[release/1.0-beta2]
        support[support/JNG-5]
        hotfix[hotfix/JNG-6]
        release3[release/1.1-beta1]
    end

    master --> develop
    develop --> feature1
    develop --> feature2
    feature1 --> develop
    feature2 --> develop
    develop --> feature3
    feature3 --> develop
    develop --> release1
    release1 --> bugfix
    bugfix --> release1
    release1 --> develop
    release1 --> master
    develop --> release2
    release2 --> support
    support --> release2
    release2 --> develop
    release2 --> master
    master --> hotfix
    hotfix --> master
    hotfix --> develop
    develop --> release3
    release3 --> develop
    release3 --> master

    style master fill:#7fff00
    style develop fill:#6495ed
    style feature1 fill:#ffd700
    style feature2 fill:#ffd700
    style feature3 fill:#ffd700
    style release1 fill:#00ffff
    style release2 fill:#00ffff
    style release3 fill:#00ffff
    style bugfix fill:#ff6347
    style support fill:#7fffd4
    style hotfix fill:#ff4500
```

## Version numbers

Version numbers are increased using semantic versioning:

* do not change version numbers on starting feature/ branches
* 2nd number in version of **develop** branch is increased when a release branch started
* do not change version numbers on bugfix/ branches - that are applied on release branches during testing before releasing it (merging to master)
* 3rd number in version of support/ branches is increased when started - it is used to support a previous release including new (minor) changes; support/ branches are merged back to release branch when update is released (without merging changes to master)
* 4th number in version of hotfix/ branches is increased when started (that are applied on both release and master branches)

### GitHub action flows

#### build.yml

```mermaid
flowchart TD
    A[Push on develop branch<br/>or<br/>PR on develop, master,<br/>increment/*, release/* branch] --> B{Commit or PR's<br/>base branch?}
    B -->|master, release/*| C[Set version from pom.xml<br/>without -SNAPSHOT]
    B -->|develop, increment/*| D[Set version<br/>major.minor.qualifier.date_commitId_branchName]
    C --> E[Build and deploy to nexus]
    D --> E
    E --> F[Create git tag v&lt;version&gt;]
    F --> G{PR or commit<br/>base branch?}
    G -->|increment/*, release/*| H[Create tag merge-pr/&lt;version&gt;]
    H --> I[Trigger merge-pr-tagged.yml]
    G -->|develop| J[Build change log]
    J --> K[Create github release<br/>prerelease with change log]
    G -->|other| L[End]
    I --> L
    K --> L
```

#### merge-pr-tagged.yml

```mermaid
flowchart TD
    A[Push on merge-pr/* tag] --> B[Get version from tag name]
    B --> C{Check version format}
    C -->|major.minor.qualifier| D[Merge PR to master]
    D --> E[Trigger create-release-on-master.yml]
    C -->|other| F[Squash PR to develop]
    F --> G[Trigger build.yml]
    E --> H[Delete tag merge-pr/&lt;version&gt;]
    G --> H
    H --> I[End]
```

#### create-release-on-master.yml

```mermaid
flowchart TD
    A[Push on master branch] --> B[Get version from tag name]
    B --> C[Build change log]
    C --> D[Create github release<br/>last with change log]
    D --> E[End]
```

#### release.yml

```mermaid
flowchart TD
    A[Manually triggered with<br/>given version<br/>auto or major.minor.qualifier] --> B{given version is}
    B -->|auto| C[Set release version<br/>from pom.xml without -SNAPSHOT]
    B -->|other| D[Set release version<br/>to given version]
    C --> E[Set next version to<br/>release version qualifier + 1]
    D --> E
    E --> F[Create PR on master<br/>with release version]
    F --> G[Trigger build.yml]
    F --> H[Create PR on develop<br/>with next version]
    H --> I[Trigger build.yml]
    G --> J[End]
    I --> J
```

## How to develop

For issue tracking we are using [JIRA](https://blackbelt.atlassian.net/jira/dashboards). Golden rule:

> **Important:** There is no commit without ticket number

So for pull request or commit `JNG-xxx` have to be presented in the commit.
