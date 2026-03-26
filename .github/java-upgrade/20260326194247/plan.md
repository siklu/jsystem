# Upgrade Plan: jsystem-parent (20260326194247)

- **Generated**: 2026-03-26 19:42:47
- **HEAD Branch**: JavaUpgrade
- **HEAD Commit ID**: a10fb61

## Available Tools

**JDKs**
- JDK 11.0.30: /usr/lib/jvm/java-11-openjdk-amd64/bin (current compile target at baseline)
- JDK 25.0.2: /usr/lib/jvm/java-25-openjdk-amd64/bin (target version, already installed — used from Step 3 onward)

**Build Tools**
- Maven 3.8.7: /usr/share/maven/bin/mvn (system Maven, running on JDK 25 — sufficient for Java 25 compilation with upgraded compiler plugin)
- No Maven wrapper present
- maven-compiler-plugin: 3.1 (Maven default) → **<TO_BE_UPGRADED>** to 3.13.0 (required for Java 25 target bytecode — upgraded in Step 1)

## Guidelines

> Note: You can add any specific guidelines or constraints for the upgrade process here if needed, bullet points are preferred. <!-- this note is for users, NEVER remove it -->

## Options

- Working branch: appmod/java-upgrade-20260326194247 <!-- user specified, NEVER remove it -->
- Run tests before and after the upgrade: true <!-- user specified, NEVER remove it -->

## Upgrade Goals

- Upgrade Java compiler source/target from 11 to 25 (all modules)
- Fix all compilation errors caused by removed/changed APIs in Java 9+ and updated third-party libraries

### Technology Stack

| Technology/Dependency | Current | Min Compatible | Why Incompatible |
| --------------------- | ------- | -------------- | ---------------- |
| Java (compiler source/target) | 11 | 25 | User requested upgrade to Java 25 |
| maven-compiler-plugin | 3.1 | 3.13.0 | Version 3.1 does not support Java 25 target bytecode |
| javax.mail (JavaMail) ⚠️ EOL | bundled/implicit | N/A | Removed from JDK; replaced by jakarta.mail-api 2.x + angus-mail runtime |
| javax.activation ⚠️ EOL | bundled/implicit | N/A | Removed from JDK since Java 11; must use jakarta.activation-api |
| javax.xml.bind (JAXB) ⚠️ EOL | bundled/implicit | N/A | Removed from JDK since Java 11; must add jakarta.xml.bind-api + jaxb-impl |
| sun.misc.BASE64Encoder/Decoder ⚠️ EOL | internal JDK | N/A | Internal API removed; replace with java.util.Base64 |
| com.sun.net.ssl.internal.ssl.Provider ⚠️ EOL | internal JDK | N/A | Internal Sun SSL API removed; use standard JSSE (auto-loaded in Java 9+) |
| org.apache.commons.httpclient 3.x ⚠️ EOL | transitive (used in code) | N/A | EOL; code uses old 3.x API while POM already declares httpclient5 |
| com.thoughtworks.qdox (JavaDocBuilder) | 2.2.0 | 2.x | JavaDocBuilder renamed to JavaProjectBuilder in qdox 2.x; getMethods() returns List not array |
| org.jfree.chart (ChartUtilities) | 1.5.6 | 1.5.x | ChartUtilities renamed to ChartUtils in JFreeChart 1.5.x |
| org.objectweb.asm (EmptyVisitor) | 9.8 | 5.x | EmptyVisitor removed in ASM 5+; replaced by MethodVisitor adapter |
| org.python.core.PyJavaClass (Jython) | 2.7.4 | 2.7.x | PyJavaClass removed in Jython 2.7.x; replaced by PyType |

### Derived Upgrades

- Add `org.eclipse.angus:angus-mail` runtime implementation for `jakarta.mail-api` (API-only jar needs runtime implementation)
- Add `jakarta.activation:jakarta.activation-api` dependency (javax.activation removed from JDK)
- Add `jakarta.xml.bind:jakarta.xml.bind-api` + `com.sun.xml.bind:jaxb-impl` (JAXB removed from JDK since Java 11)
- Upgrade `maven-compiler-plugin` from 3.1 to 3.13.0 in `jsystem-parent/pom.xml` (required for Java 25 target bytecode)
- Update `maven.compiler.source` and `maven.compiler.target` from `11` → `25` in `jsystem-parent/pom.xml`
- Apply pending version bump changes: `6.1.17-SIKLU` → `6.1.18-SIKLU` across all module POMs (restore uncommitted stash changes)
- Migrate `DifidoClient.java` and `UploadRunner.java` fully from commons-httpclient 3.x to httpclient5 API

## Upgrade Steps

- **Step 1: Setup Environment**
  - **Rationale**: JDK 25 is already installed. Upgrade maven-compiler-plugin to 3.13.0 in the parent POM to support Java 25 bytecode compilation.
  - **Changes to Make**:
    - [ ] Add explicit `maven-compiler-plugin` version `3.13.0` to the `<build><plugins>` section of `jsystem-parent/pom.xml`
  - **Verification**:
    - Command: `java -version && mvn -version`
    - Expected: JDK 25.0.2 available, Maven 3.8.7 present

---

- **Step 2: Setup Baseline**
  - **Rationale**: Establish pre-upgrade compile and test results to measure upgrade success against.
  - **Changes to Make**:
    - [ ] Run baseline compilation with current settings (Java 11 compile target, JDK 25 JVM)
    - [ ] Run baseline tests
  - **Verification**:
    - Command: `JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64 mvn -f jsystem-parent/pom.xml clean compile`
    - JDK: /usr/lib/jvm/java-25-openjdk-amd64
    - Expected: FAILURE — document 12-file, ~200-error baseline in progress.md

---

- **Step 3: Upgrade Java Compiler Target to 25 + Apply Version Bump**
  - **Rationale**: Set the compiler source/target to 25. Also restore pending version bump (6.1.17 → 6.1.18) and archetype POM updates from the stashed working state.
  - **Changes to Make**:
    - [ ] Update `maven.compiler.source` and `maven.compiler.target` to `25` in `jsystem-parent/pom.xml`
    - [ ] Apply version bump across all module POMs (`6.1.17-SIKLU` → `6.1.18-SIKLU`) by restoring git stash contents
    - [ ] Update archetype resource POMs to compiler source/target `25`
  - **Verification**:
    - Command: `JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64 mvn -f jsystem-parent/pom.xml clean test-compile`
    - JDK: /usr/lib/jvm/java-25-openjdk-amd64
    - Expected: FAILURE (same code errors, now with Java 25 target — expected at this stage)

---

- **Step 4: Fix javax.* → jakarta.* API Migrations**
  - **Rationale**: javax.mail, javax.activation, and javax.xml.bind were removed from the JDK after Java 8. Code must use Jakarta EE equivalents with added Maven dependencies.
  - **Changes to Make**:
    - [ ] Migrate `MailUtil.java`: `javax.mail.*`/`javax.activation.*` → `jakarta.mail.*`/`jakarta.activation.*`; remove `com.sun.net.ssl.internal.ssl.Provider` call
    - [ ] Migrate `MailMessage.java`: `javax.mail.*` → `jakarta.mail.*`
    - [ ] Fix `JTestContainer.java`: `javax.mail.MethodNotSupportedException` → `jakarta.mail.MethodNotSupportedException`
    - [ ] Fix `JUnitReporter.java`: `javax.xml.bind.*` → `jakarta.xml.bind.*`
    - [ ] Add `jakarta.activation-api`, `angus-mail`, `jakarta.xml.bind-api`, `jaxb-impl` to `jsystem-parent/pom.xml` dependencyManagement and `jsystemCore/pom.xml`
  - **Verification**:
    - Command: `JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64 mvn -f jsystem-parent/pom.xml clean test-compile`
    - JDK: /usr/lib/jvm/java-25-openjdk-amd64
    - Expected: javax.mail/activation/bind errors resolved; remaining: sun.misc, qdox, JFreeChart, ASM, Jython, httpclient errors

---

- **Step 5: Fix Removed Internal JDK APIs**
  - **Rationale**: sun.misc.BASE64Encoder/Decoder and com.sun.net.ssl.internal.ssl.Provider are internal APIs removed in Java 9+.
  - **Changes to Make**:
    - [ ] Fix `Encryptor.java`: replace `sun.misc.BASE64Encoder().encode(enc)` → `Base64.getEncoder().encodeToString(enc)` and `sun.misc.BASE64Decoder().decodeBuffer(str)` → `Base64.getDecoder().decode(str)`
    - [ ] Fix `MailUtil.java`: remove `Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider())` line (JSSE providers are auto-loaded in Java 9+)
  - **Verification**:
    - Command: `JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64 mvn -f jsystem-parent/pom.xml clean test-compile`
    - JDK: /usr/lib/jvm/java-25-openjdk-amd64
    - Expected: sun.misc and sun SSL errors resolved

---

- **Step 6: Fix Third-Party Library API Incompatibilities**
  - **Rationale**: qdox 2.x, JFreeChart 1.5.x, ASM 9.x, Jython 2.7.x, and httpclient5 all have breaking API changes. All 8 remaining error files are addressed here.
  - **Changes to Make**:
    - [ ] Fix `HtmlCodeWriter.java`: rename `JavaDocBuilder` → `JavaProjectBuilder`; update `getMethods()` return type from `JavaMethod[]` to `List<JavaMethod>`
    - [ ] Fix `Graph.java` and `BarGraph.java`: `ChartUtilities` → `ChartUtils` (JFreeChart 1.5 rename)
    - [ ] Fix `AsmParameterNameLoader.java`: replace `EmptyVisitor` with `MethodVisitor` using `Opcodes.ASM9` API version (ASM 9.x removed EmptyVisitor)
    - [ ] Fix `JythonScriptExecutor.java`: replace `PyJavaClass` cast with `PyType` (removed in Jython 2.7)
    - [ ] Rewrite `DifidoClient.java`: migrate all methods from `org.apache.commons.httpclient` 3.x to `org.apache.hc.client5` 5.x
    - [ ] Rewrite `UploadRunner.java`: migrate from `org.apache.commons.httpclient` 3.x to `org.apache.hc.client5` 5.x
  - **Verification**:
    - Command: `JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64 mvn -f jsystem-parent/pom.xml clean test-compile`
    - JDK: /usr/lib/jvm/java-25-openjdk-amd64
    - Expected: Compilation SUCCESS — all 12 error files resolved

---

- **Step 7: Final Validation**
  - **Rationale**: Verify all upgrade goals are met: Java 25 target, all code compiles, all tests pass.
  - **Changes to Make**:
    - [ ] Verify `maven.compiler.source/target` = 25 in all relevant POMs
    - [ ] Resolve all remaining TODOs
    - [ ] Clean rebuild with JDK 25
    - [ ] Fix any remaining compilation errors
    - [ ] Run full test suite and fix ALL failing tests (iterative fix loop until 100% pass)
  - **Verification**:
    - Command: `JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64 mvn -f jsystem-parent/pom.xml clean test`
    - JDK: /usr/lib/jvm/java-25-openjdk-amd64
    - Expected: Compilation SUCCESS + 100% tests pass

## Key Challenges

- **javax.* to jakarta.* Namespace Migration**
  - **Challenge**: Multiple Java EE APIs (javax.mail, javax.activation, javax.xml.bind) were removed from the JDK after Java 8. Code still uses the old javax.* namespace across 4 files.
  - **Strategy**: Replace imports with jakarta.* equivalents; add Jakarta EE API jars + runtime implementations as Maven dependencies. `jakarta.mail-api` is already declared but needs `angus-mail` runtime. JAXB needs both API (`jakarta.xml.bind-api`) and implementation (`jaxb-impl`).

- **Old HTTPClient 3.x (commons-httpclient) Removal**
  - **Challenge**: `DifidoClient.java` and `UploadRunner.java` use `org.apache.commons.httpclient` (3.x EOL), which is not declared as a Maven dependency. Project already migrated POM to httpclient5 but not the code.
  - **Strategy**: Fully rewrite the affected methods using `org.apache.hc.client5` which is already declared in the parent POM.

- **ASM EmptyVisitor Removal**
  - **Challenge**: `AsmParameterNameLoader.java` extends `EmptyVisitor` removed in ASM 5 (project uses ASM 9.8). Used to discover parameter names from bytecode using the visitor pattern.
  - **Strategy**: Replace `EmptyVisitor` with `MethodVisitor` using `Opcodes.ASM9` API level constant. Override only needed visitor methods.

- **Jython PyJavaClass Removal**
  - **Challenge**: `JythonScriptExecutor.java` casts to `PyJavaClass` which was removed in Jython 2.7.x (project uses 2.7.4).
  - **Strategy**: Replace `PyJavaClass` cast with `PyType` — the current equivalent for Java class wrappers in Jython 2.7.x.
