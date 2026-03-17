# judo-meta-query

[![Build](https://github.com/BlackBeltTechnology/judo-meta-query/actions/workflows/build.yml/badge.svg?branch=develop)](https://github.com/BlackBeltTechnology/judo-meta-query/actions/workflows/build.yml)

## Introduction

**judo-meta-query** defines the Query metamodel — an EMF (Eclipse Modeling Framework) Ecore model that represents expression-based transformation logic for queries. This is **not** an SQL model; instead, it captures the structural and logical relationships needed to later generate RDBMS queries.

The project can be consumed in three ways:

- As an **Eclipse plugin** with features and update site
- As a **standalone Java library** (Maven artifact)
- As an **OSGi bundle** for non-Eclipse service containers

This project is a building block of the [judo-community](https://github.com/BlackBeltTechnology/judo-community) aggregator project. See that repository for how this module fits into the broader JUDO ecosystem.

## Query Metamodel Overview

The metamodel is defined in [`model/model/query.ecore`](model/model/query.ecore). Below is a summary of the core concepts and how they relate.

### Class Hierarchy

```mermaid
classDiagram
    class NavigationBase {
        <<abstract>>
        +joins : Join[*]
        +subSelects : SubSelect[*]
    }
    class FeatureHolder {
        <<abstract>>
        +features : Feature[*]
    }
    class Node {
        <<abstract>>
        +alias : String
        +filters : Filter[*]
        +orderBys : OrderBy[*]
        +getType() EClass
    }
    class Select {
        +from : EClass
        +targets : Target[*]
        +mainTarget : Target
        +singleColumnedSelect : boolean
        +getAllJoins() Join[]
        +isAggregated() boolean
    }
    class SubSelect {
        +transferRelation : EReference
        +select : Select
        +base : Node
        +embeddedSelect : Select
        +limit : Integer
        +offset : Integer
    }
    class Join {
        <<abstract>>
        +partner : Node
        +getBase() Node
        +getAllJoins() Join[]
    }
    class ReferencedJoin {
        +reference : EReference
    }
    class SubSelectJoin {
        +subSelect : SubSelect
    }
    class ContainerJoin {
        +references : EReference[*]
    }
    class CastJoin {
        +type : EClass
    }
    class CustomJoin {
        +type : EClass
        +transferRelation : EReference
        +navigationSql : String
    }
    class Feature {
        <<abstract>>
        +targetMappings : FeatureTargetMapping[*]
        +filters : Filter[*]
        +orderBys : OrderBy[*]
    }
    class FeatureWithNode {
        +node : Node
    }
    class Attribute {
        +sourceAttribute : EAttribute
    }
    class Constant {
        +value : Object
    }
    class Function {
        +signature : FunctionSignature
        +parameters : FunctionParameter[*]
    }
    class Variable {
        +category : String
        +name : String
        +type : EDataType
    }
    class Target {
        +index : int
        +type : EClass
        +node : Node
        +referencedTargets : ReferencedTarget[*]
    }
    class Filter {
        +feature : Feature
    }
    class OrderBy {
        +descending : boolean
        +feature : Feature
    }

    NavigationBase <|-- Node
    FeatureHolder <|-- Node
    Node <|-- Select
    Node <|-- SubSelect
    Node <|-- Join
    Node <|-- Filter
    Node <|-- OrderBy
    Join <|-- ReferencedJoin
    Join <|-- SubSelectJoin
    Join <|-- ContainerJoin
    Join <|-- CastJoin
    Join <|-- CustomJoin
    Feature <|-- FeatureWithNode
    Feature <|-- Constant
    Feature <|-- Function
    Feature <|-- Variable
    Feature <|-- SubSelectFeature
    Feature <|-- EntityTypeName
    FeatureWithNode <|-- Attribute
    FeatureWithNode <|-- IdAttribute
    FeatureWithNode <|-- TypeAttribute
    Select --> Target : targets
    Select --> Node : from
    Join --> Node : partner
    Feature --> FeatureTargetMapping : targetMappings
    Function --> FunctionParameter : parameters
```

### Key Concepts

| Concept | Description |
|---------|-------------|
| **Node** | Abstract base for all query graph nodes. Returns an `EClass` entity type via `getType()`. Contains filters, orderBys, and an alias. |
| **Select** | Root query for an entity. Has a `from` EClass, multiple `Target` projections, and one `mainTarget`. Each transfer object has one relevant Select. |
| **SubSelect** | A runtime query that navigates relations via `transferRelation`. The `base` field is the navigation starting point. Can contain an `embeddedSelect` as a workaround when a Select isn't available from transfer objects. Supports `limit`/`offset` for pagination. |
| **Join** | Abstract join specification. `getBase()` recursively walks the `partner` chain to find the root node. Five concrete types: `ReferencedJoin` (follows an EReference), `SubSelectJoin` (wraps a SubSelect), `ContainerJoin` (navigates containment), `CastJoin` (type casting), `CustomJoin` (custom SQL navigation). |
| **Feature** | Abstract class representing something a Select or Join returns (attributes, calculated values, constants). Mapped to result columns via `FeatureTargetMapping`. |
| **Target** | Projection descriptor that defines how to represent query results. Has an `index`, optional `type` (EClass), and `referencedTargets` for navigating to related targets. |
| **Filter** | Where-clause condition node, linked to a `Feature` that provides the filter expression. |
| **Function** | Represents a function call with a `FunctionSignature` enum (118+ operations covering logic, comparison, arithmetic, string, date, time, and type operations) and typed `FunctionParameter`s. |
| **Variable** | A query variable with `category`, `name`, and `type` — used for parameterized queries. |
| **SubSelectFeature** | A Feature used in "where" clauses that references a SubSelect and an aggregation Feature. |
| **EntityTypeName** | Used with inheritance to reference entity types by name. |

### Module Dependency Graph

```mermaid
graph TD
    subgraph Eclipse Distribution
        feature["feature<br/><i>eclipse-feature</i>"]
        site["site<br/><i>eclipse-repository</i>"]
    end
    subgraph Core
        model["model<br/><i>eclipse-plugin</i>"]
    end
    subgraph OSGi Distribution
        osgi["osgi<br/><i>maven bundle</i>"]
    end
    subgraph Testing
        model-test["model-test<br/><i>JUnit 5</i>"]
        osgi-itest["osgi-itest<br/><i>Pax Exam + Karaf</i>"]
    end

    model-test -.->|tests| model
    osgi -->|repackages| model
    osgi-itest -.->|tests| osgi
    feature -->|includes| model
    site -->|publishes| feature
```

## Build Commands

> **Prerequisites:** Java 21 JDK, Maven 3.9.4+. A Maven wrapper (`./mvnw`) is included.

```bash
# Full build (all modules)
./mvnw clean install

# Run tests only
./mvnw clean test

# Run a single test class
./mvnw test -pl model-test -Dtest=QueryValidationTest

# Skip tests
./mvnw clean install -DskipTests

# Update Eclipse site category versions
mvn clean install -P update-category-versions -f site/pom.xml
```

## Contributing

Everyone is welcome to contribute to JUDO! See [CONTRIBUTING.md](CONTRIBUTING.md) for development setup, code structure details, and submission guidelines.

## License

This project is licensed under the [Eclipse Public License - v 2.0](https://www.eclipse.org/legal/epl-2.0/).
