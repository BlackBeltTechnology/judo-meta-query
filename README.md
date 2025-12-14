# judo-meta-query

[![Build Status](https://github.com/BlackBeltTechnology/judo-meta-query/actions/workflows/build.yml/badge.svg?branch=develop)](https://github.com/BlackBeltTechnology/judo-meta-query/actions/workflows/build.yml)

## Introduction

This repository contains the Query meta model.

It acts as an Eclipse plugin with features and sites, can be used standalone and in standard OSGi (without Eclipse).

This is not an SQL model, but a model representing transformation logic for Expression-based queries. The output of this is used later for RDBMS query generation.

## Context

This project is a building block of the [judo-community](https://github.com/BlackBeltTechnology/judo-community) aggregator project. In order to better understand how this module fits into our ecosystem, please check the corresponding documentation!

## Contributing to the project

Everyone is welcome to contribute to JUDO! As a starter, please read the corresponding [CONTRIBUTING](CONTRIBUTING.md) guide for details!

## License

This project is licensed under the [Eclipse Public License - v 2.0](https://www.eclipse.org/legal/epl-2.0/).

## Query meta model notes

### Node
- `getType()`: returns EClass type representing an entity
- not all of its relations can be used everywhere

### Select: actual "select" for an entity
- `from`: the (EClass) entity to be selected
- each transferobject has 1 relevant select

### Subselects
- in runtime it is the query
- `transferRelation`: a relation to be used for subselect
- used to navigate on relations
- `embedded select`: workaround to be able to contain a select if it's not available from TO-s
- `base`: navigation start

### SubSelectFeature
- subselects used in "where" part

### Targets: projection
- describes how to represent the result returned from the query

### Join
- `getBase()`: to what it is joined (recursively queries partners)

### Feature
- "Something" (possibly an attribute, calculated value, constants etc.) that the select or join returns
- `target mapping`: the attribute type of the result from feature

### FeatureWithNode
- features that have strong relation to entities
- `<|-- Attribute`: entity attribute
- `<|-- idAttribute`: entity id
- `<|-- TypeAttribute`: entity type, version, create time etc.

### EntityTypeName
- used with inheritance

### Variable
- category and name + type -> e.g.: `String!getVariable(..., ...)`
