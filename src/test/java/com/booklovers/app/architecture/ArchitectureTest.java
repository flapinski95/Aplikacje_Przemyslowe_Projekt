package com.booklovers.app.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

public class ArchitectureTest {

    private static final String CONTROLLER_PACKAGE = "com.booklovers.app.controller";
    private static final String SERVICE_PACKAGE = "com.booklovers.app.service";
    private static final String REPOSITORY_PACKAGE = "com.booklovers.app.repository";
    private static final String MODEL_PACKAGE = "com.booklovers.app.model";
    private static final String ENTITY_PACKAGE = "com.booklovers.app.model";
    private static final String DTO_PACKAGE = "com.booklovers.app.dto";

    private final JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.booklovers.app");

    @Test
    void controllersShouldNotAccessRepositoriesDirectly() {

        ArchRule rule = noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..repository..")
                .because("Controllers should rely on Services, not Repositories directly");

        rule.check(importedClasses);
    }

    @Test
    void servicesShouldBeInServicePackage() {
        ArchRule rule = classes()
                .that().areAnnotatedWith("org.springframework.stereotype.Service")
                .should().resideInAnyPackage("..service..", "..security..")
                .because("Services must be in service package or security package");

        rule.check(importedClasses);
    }

    @Test
    void controllersShouldBeInControllerPackage() {
        ArchRule rule = classes()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .or().areAnnotatedWith("org.springframework.stereotype.Controller")
                .should().resideInAPackage("..controller..")
                .because("Controllers must be in controller package");

        rule.check(importedClasses);
    }

    @Test
    void repositoriesShouldBeInRepositoryPackage() {
        ArchRule rule = classes()
                .that().areAnnotatedWith("org.springframework.stereotype.Repository")
                .should().resideInAPackage("..repository..")
                .because("Repositories must be in repository package");

        rule.check(importedClasses);
    }

    @Test
    void entitiesShouldNotDependOnControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..model..")
                .should().dependOnClassesThat().resideInAPackage("..controller..")
                .because("Entities should not depend on controllers");

        rule.check(importedClasses);
    }

    @Test
    void entitiesShouldNotDependOnServices() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..model..")
                .should().dependOnClassesThat().resideInAPackage("..service..")
                .because("Entities should not depend on services");

        rule.check(importedClasses);
    }

    @Test
    void repositoriesShouldNotDependOnControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..repository..")
                .should().dependOnClassesThat().resideInAPackage("..controller..")
                .because("Repositories should not depend on controllers");

        rule.check(importedClasses);
    }
}
