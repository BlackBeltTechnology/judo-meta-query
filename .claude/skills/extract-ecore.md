---
name: Extract Ecore Model Elements
description: Extract model elements (classes, attributes, references, operations) from EMF Ecore metamodel files
---

# Extract Ecore Model Elements

This skill extracts model structure from EMF Ecore metamodel files for analysis.

## Target File

The primary ecore file in this project is:
- `model/model/esm.ecore`

## Ecore XML Structure

Ecore files are XML-based with the following key elements:

### Package Structure
```xml
<ecore:EPackage name="esm" nsURI="http://blackbelt.hu/judo/meta/esm" nsPrefix="esm">
  <eSubpackages name="namespace" nsURI="http://blackbelt.hu/judo/meta/esm/namespace" nsPrefix="namespace">
    ...
  </eSubpackages>
</ecore:EPackage>
```

### Class Definition (EClass)
```xml
<eClassifiers xsi:type="ecore:EClass" name="Model" eSuperTypes="#//namespace/Namespace #//namespace/NamedElement">
  ...
</eClassifiers>
```

Key attributes:
- `name` - Class name
- `abstract="true"` - Abstract class marker
- `eSuperTypes` - Inheritance (e.g., `#//namespace/Element`)

### Attribute Definition (EAttribute)
```xml
<eStructuralFeatures xsi:type="ecore:EAttribute" name="name" lowerBound="1" 
    eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
```

Key attributes:
- `name` - Attribute name
- `eType` - Data type (EString, EBoolean, EInt, custom types)
- `lowerBound` - Minimum cardinality (0 or 1)
- `upperBound` - Maximum cardinality (1 or -1 for many)
- `defaultValueLiteral` - Default value

### Reference Definition (EReference)
```xml
<eStructuralFeatures xsi:type="ecore:EReference" name="elements" upperBound="-1" 
    eType="#//namespace/NamespaceElement" containment="true"/>
```

Key attributes:
- `name` - Reference name
- `eType` - Target type (e.g., `#//structure/Class`)
- `containment="true"` - Composition relationship
- `eOpposite` - Bidirectional reference partner
- `lowerBound`/`upperBound` - Cardinality

### Operation Definition (EOperation)
```xml
<eOperations name="getFQName" lowerBound="1" eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>

<eOperations name="getElementByName" eType="#//namespace/NamedElement">
  <eParameters name="name" eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
</eOperations>
```

Key attributes:
- `name` - Operation name
- `eType` - Return type
- `eParameters` - Method parameters

### Data Type Definition (EDataType)
```xml
<eClassifiers xsi:type="ecore:EDataType" name="RegExp" instanceClassName="java.lang.String"/>
```

### Enumeration Definition (EEnum)
```xml
<eClassifiers xsi:type="ecore:EEnum" name="MemberType">
  <eLiterals name="STORED" value="0"/>
  <eLiterals name="DERIVED" value="1"/>
  <eLiterals name="MAPPED" value="2"/>
</eClassifiers>
```

## ESM Metamodel Packages

The ESM metamodel contains these packages:

| Package | URI | Purpose |
|---------|-----|---------|
| `esm` | `http://blackbelt.hu/judo/meta/esm` | Root package |
| `namespace` | `.../esm/namespace` | Named elements, packages, model |
| `type` | `.../esm/type` | Primitive types, enumerations |
| `structure` | `.../esm/structure` | Classes, attributes, relations |
| `operation` | `.../esm/operation` | Operations, parameters |
| `accesspoint` | `.../esm/accesspoint` | Actors, access points |
| `measure` | `.../esm/measure` | Measurements, units |
| `ui` | `.../esm/ui` | UI components (forms, tables, views) |
| `expression` | `.../esm/expression` | Expression types |
| `script` | `.../esm/script` | Script support |

## Extraction Instructions

1. **Extract Package Hierarchy**:
   - List all packages with their URIs and prefixes
   - Show nesting structure

2. **Extract Classes**:
   - Name and abstract flag
   - Supertype references (resolve `#//package/Class` format)
   - Count of attributes, references, operations

3. **Extract Class Members**:
   For each class, extract:
   - Attributes with types and cardinality
   - References with targets and containment flag
   - Operations with signatures

4. **Output Formats**:

   **Package Overview**:
   ```
   | Package | Classes | Description |
   |---------|---------|-------------|
   | namespace | 8 | Named elements, packages |
   | structure | 25 | Classes, attributes, relations |
   ```

   **Class Details**:
   ```
   ### Class: EntityType
   - Package: structure
   - Abstract: false
   - Extends: TransferObjectType
   
   **Attributes:**
   | Name | Type | Required | Default |
   |------|------|----------|---------|
   | abstract | EBoolean | yes | - |
   
   **References:**
   | Name | Target | Cardinality | Containment |
   |------|--------|-------------|-------------|
   | constraints | InvariantConstraint | 0..* | yes |
   
   **Operations:**
   | Name | Return Type | Parameters |
   |------|-------------|------------|
   | isMapped | EBoolean | - |
   ```

## Key Classes in ESM

Important classes to understand:

- `Model` - Root element of ESM models
- `Package` - Namespace container
- `EntityType` - Persistent entity definition
- `TransferObjectType` - DTO/transfer object
- `DataMember` - Attribute definition
- `RelationFeature` - Relation definition
- `Operation` - Operation/method definition
- `ActorType` - Access point/actor definition

## Usage

To use this skill, read the ecore file and extract elements matching the patterns above. Present the metamodel structure hierarchically or filter by package/class as needed.
