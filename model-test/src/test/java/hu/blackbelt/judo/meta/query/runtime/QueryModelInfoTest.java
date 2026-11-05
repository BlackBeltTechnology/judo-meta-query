package hu.blackbelt.judo.meta.query.runtime;

/*-
 * #%L
 * Judo :: Query :: Model
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import hu.blackbelt.judo.meta.query.AbstractQueryValidationTest;
import hu.blackbelt.judo.meta.query.DataQueryEntry;
import hu.blackbelt.judo.meta.query.EntityTypeExpressionsEntry;
import hu.blackbelt.judo.meta.query.GetterAttributeEntry;
import hu.blackbelt.judo.meta.query.GetterReferenceEntry;
import hu.blackbelt.judo.meta.query.NavigationEntry;
import hu.blackbelt.judo.meta.query.QueryEntry;
import hu.blackbelt.judo.meta.query.QueryModelInfo;
import hu.blackbelt.judo.meta.query.Select;
import hu.blackbelt.judo.meta.query.SubSelect;
import hu.blackbelt.judo.meta.query.Target;
import hu.blackbelt.judo.meta.query.ValidatorType;
import hu.blackbelt.judo.meta.query.support.QueryModelResourceSupport;
import hu.blackbelt.judo.meta.query.util.builder.QueryBuilders;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static hu.blackbelt.judo.meta.query.support.QueryModelResourceSupport.SaveArguments.querySaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.query.support.QueryModelResourceSupport.LoadArguments.queryLoadArgumentsBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the persisted index root {@link QueryModelInfo}: a query resource holding query roots and a
 * {@code QueryModelInfo} that references ASM objects and expression objects of other resources is valid,
 * passes both query validators and survives a save / load round trip with resolved references.
 */
@Slf4j
public class QueryModelInfoTest extends AbstractQueryValidationTest {

    @TempDir
    File tempDir;

    private ResourceSet resourceSet;
    private Resource asmResource;
    private Resource expressionResource;
    private EClass entity;
    private EAttribute nameAttribute;
    private EAttribute getterAttribute;
    private EReference friendReference;
    private EReference getterReference;
    private EAnnotation attributeExpression;
    private EAnnotation referenceExpression;
    private QueryModelInfo info;

    @BeforeEach
    void buildModels() {
        resourceSet = queryModelSupport.getResourceSet();
        queryModelSupport.getResource().setURI(URI.createFileURI(new File(tempDir, "query.model").getAbsolutePath()));

        // ASM stand-in: a dynamic package in its own resource
        EPackage asmPackage = EcoreFactory.eINSTANCE.createEPackage();
        asmPackage.setName("asm");
        asmPackage.setNsPrefix("asm");
        asmPackage.setNsURI("http://test/asm");
        entity = EcoreFactory.eINSTANCE.createEClass();
        entity.setName("Entity");
        nameAttribute = attribute("name");
        getterAttribute = attribute("derivedName");
        entity.getEStructuralFeatures().add(nameAttribute);
        entity.getEStructuralFeatures().add(getterAttribute);
        friendReference = reference("friend");
        getterReference = reference("derivedFriend");
        entity.getEStructuralFeatures().add(friendReference);
        entity.getEStructuralFeatures().add(getterReference);
        asmPackage.getEClassifiers().add(entity);
        asmResource = resourceSet.createResource(URI.createFileURI(new File(tempDir, "asm.model").getAbsolutePath()));
        asmResource.getContents().add(asmPackage);

        // Expression stand-in: objects of a second resource (the Query metamodel only knows them as EObject)
        attributeExpression = annotation("attribute-expression");
        referenceExpression = annotation("reference-expression");
        expressionResource = resourceSet.createResource(URI.createFileURI(new File(tempDir, "expression.model").getAbsolutePath()));
        expressionResource.getContents().add(attributeExpression);
        expressionResource.getContents().add(referenceExpression);

        // Query roots: a Select and two helper SubSelects, all roots of the query resource
        Select select = QueryBuilders.newSelectBuilder().withAlias("s01").withFrom(entity).build();
        Target target = QueryBuilders.newTargetBuilder().withIndex(1).withType(entity).build();
        select.getTargets().add(target);
        select.setMainTarget(target);
        SubSelect navigation = QueryBuilders.newSubSelectBuilder().withAlias("ss01").withSelect(select).build();
        SubSelect dataQuery = QueryBuilders.newSubSelectBuilder().withAlias("ss02").withSelect(select).build();

        info = QueryBuilders.newQueryModelInfoBuilder()
                .withFormatVersion(1)
                .withModelName("test")
                .withGenerator("judo-meta-query-test/1.0")
                .withAsmDigest("asm-digest")
                .withMeasureDigest("measure-digest")
                .withExpressionDigest("expression-digest")
                .withNextSourceIndex(7)
                .withNextTargetIndex(9)
                .withQueries(QueryBuilders.newQueryEntryBuilder().withType(entity).withSelect(select))
                .withNavigations(QueryBuilders.newNavigationEntryBuilder().withReference(friendReference).withSubSelect(navigation))
                .withDataQueries(QueryBuilders.newDataQueryEntryBuilder().withAttribute(nameAttribute).withSubSelect(dataQuery))
                .withOrderedTransferRelations(friendReference, getterReference)
                .withStaticEntityAttributes(getterAttribute)
                .withStaticEntityReferences(getterReference)
                .withEntityTypeExpressions(QueryBuilders.newEntityTypeExpressionsEntryBuilder()
                        .withEntityType(entity)
                        .withGetterAttributes(QueryBuilders.newGetterAttributeEntryBuilder()
                                .withAttribute(getterAttribute).withExpression(attributeExpression))
                        .withGetterReferences(QueryBuilders.newGetterReferenceEntryBuilder()
                                .withReference(getterReference).withExpression(referenceExpression)))
                .build();

        queryModelSupport.addContent(select);
        queryModelSupport.addContent(navigation);
        queryModelSupport.addContent(dataQuery);
        queryModelSupport.addContent(info);
    }

    private static EAttribute attribute(String name) {
        EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
        attribute.setName(name);
        attribute.setEType(EcorePackage.Literals.ESTRING);
        return attribute;
    }

    private EReference reference(String name) {
        EReference reference = EcoreFactory.eINSTANCE.createEReference();
        reference.setName(name);
        reference.setEType(entity);
        return reference;
    }

    private static EAnnotation annotation(String source) {
        EAnnotation annotation = EcoreFactory.eINSTANCE.createEAnnotation();
        annotation.setSource(source);
        return annotation;
    }

    /** TP-17: the query resource with the index root is valid. */
    @Test
    void testQueryModelWithInfoRootIsValid() {
        assertTrue(queryModelSupport.isValid(), queryModelSupport.getDiagnosticsAsString());
    }

    /** The store's query validators (EVL and Zeta) accept a resource with the index root. */
    @ParameterizedTest(name = "{0}")
    @EnumSource(ValidatorType.class)
    void testValidatorsAcceptInfoRoot(ValidatorType validatorType) throws Exception {
        runValidationExpectingNoIssues(validatorType);
    }

    @Test
    void testEntriesAreContainedAndTargetsAreReferenced() {
        assertEquals(1, queryModelSupport.getResource().getContents().stream().filter(QueryModelInfo.class::isInstance).count());
        assertEquals(3, queryModelSupport.getResource().getContents().stream().filter(o -> !(o instanceof QueryModelInfo)).count());

        QueryEntry queryEntry = info.getQueries().get(0);
        assertSame(info, queryEntry.eContainer());
        assertTrue(queryEntry.getSelect().eContainer() == null && queryEntry.getSelect().eResource() == queryModelSupport.getResource(),
                "Select stays a root of the query resource");
        assertSame(info, info.getNavigations().get(0).eContainer());
        assertSame(info, info.getDataQueries().get(0).eContainer());
        EntityTypeExpressionsEntry typeEntry = info.getEntityTypeExpressions().get(0);
        assertSame(info, typeEntry.eContainer());
        assertSame(typeEntry, typeEntry.getGetterAttributes().get(0).eContainer());
        assertSame(typeEntry, typeEntry.getGetterReferences().get(0).eContainer());
        assertSame(asmResource, queryEntry.getType().eResource());
        assertSame(expressionResource, typeEntry.getGetterAttributes().get(0).getExpression().eResource());
    }

    /** TP-17: save, reload into a separate ResourceSet, valid, equal and fully resolved. */
    @Test
    void testRoundTripKeepsValidityEqualityAndResolvedReferences() throws Exception {
        asmResource.save(QueryModelResourceSupport.getQueryModelDefaultSaveOptions());
        expressionResource.save(QueryModelResourceSupport.getQueryModelDefaultSaveOptions());
        queryModelSupport.saveQuery(querySaveArgumentsBuilder());

        ResourceSet loadedSet = QueryModelResourceSupport.createQueryResourceSet();
        Resource loadedAsm = loadedSet.getResource(asmResource.getURI(), true);
        Resource loadedExpression = loadedSet.getResource(expressionResource.getURI(), true);
        QueryModelResourceSupport loaded = QueryModelResourceSupport.loadQuery(queryLoadArgumentsBuilder()
                .uri(queryModelSupport.getResource().getURI())
                .resourceSet(loadedSet)
                .validateModel(true));

        assertTrue(loaded.isValid(), loaded.getDiagnosticsAsString());
        EcoreUtil.resolveAll(loadedSet);

        // equality of the query content (queries, navigations, data queries, entries, stamps)
        assertTrue(new EcoreUtil.EqualityHelper().equals(queryModelSupport.getResource().getContents(), loaded.getResource().getContents()),
                "loaded query resource equals the saved one");

        QueryModelInfo loadedInfo = loaded.getStreamOf(QueryModelInfo.class).findFirst().orElseThrow();
        assertEquals(1, loadedInfo.getFormatVersion());
        assertEquals("test", loadedInfo.getModelName());
        assertEquals("judo-meta-query-test/1.0", loadedInfo.getGenerator());
        assertEquals("asm-digest", loadedInfo.getAsmDigest());
        assertEquals("measure-digest", loadedInfo.getMeasureDigest());
        assertEquals("expression-digest", loadedInfo.getExpressionDigest());
        assertEquals(7, loadedInfo.getNextSourceIndex());
        assertEquals(9, loadedInfo.getNextTargetIndex());

        // references resolve into the registered resources
        EClass loadedEntity = (EClass) ((EPackage) loadedAsm.getContents().get(0)).getEClassifier("Entity");
        QueryEntry queryEntry = loadedInfo.getQueries().get(0);
        assertSame(loadedEntity, queryEntry.getType());
        assertSame(loadedEntity.getEStructuralFeature("friend"), loadedInfo.getNavigations().get(0).getReference());
        assertSame(loadedEntity.getEStructuralFeature("name"), loadedInfo.getDataQueries().get(0).getAttribute());
        assertEquals(List.of(loadedEntity.getEStructuralFeature("friend"), loadedEntity.getEStructuralFeature("derivedFriend")),
                loadedInfo.getOrderedTransferRelations());
        assertEquals(List.of(loadedEntity.getEStructuralFeature("derivedName")), loadedInfo.getStaticEntityAttributes());
        assertEquals(List.of(loadedEntity.getEStructuralFeature("derivedFriend")), loadedInfo.getStaticEntityReferences());
        assertSame(loaded.getResource(), queryEntry.getSelect().eResource());
        assertSame(loaded.getResource(), loadedInfo.getNavigations().get(0).getSubSelect().eResource());
        assertSame(loaded.getResource(), loadedInfo.getDataQueries().get(0).getSubSelect().eResource());

        EntityTypeExpressionsEntry typeEntry = loadedInfo.getEntityTypeExpressions().get(0);
        assertSame(loadedEntity, typeEntry.getEntityType());
        GetterAttributeEntry attributeEntry = typeEntry.getGetterAttributes().get(0);
        GetterReferenceEntry referenceEntry = typeEntry.getGetterReferences().get(0);
        assertSame(loadedEntity.getEStructuralFeature("derivedName"), attributeEntry.getAttribute());
        assertSame(loadedExpression.getContents().get(0), attributeEntry.getExpression());
        assertSame(loadedEntity.getEStructuralFeature("derivedFriend"), referenceEntry.getReference());
        assertSame(loadedExpression.getContents().get(1), referenceEntry.getExpression());

        // no unresolved proxy anywhere in the loaded set
        List<EObject> proxies = new ArrayList<>();
        for (Resource resource : loadedSet.getResources()) {
            for (Iterator<EObject> it = resource.getAllContents(); it.hasNext(); ) {
                EObject object = it.next();
                for (EObject referenced : object.eCrossReferences()) {
                    if (referenced.eIsProxy()) {
                        proxies.add(object);
                    }
                }
                assertFalse(object.eIsProxy());
            }
        }
        assertTrue(proxies.isEmpty(), "unresolved proxies: " + proxies);
        assertNotNull(loadedInfo.eResource());
    }
}
