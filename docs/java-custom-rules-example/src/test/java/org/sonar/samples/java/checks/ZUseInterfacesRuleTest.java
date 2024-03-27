/*
 * Copyright (C) 2012-2024 SonarSource SA - mailto:info AT sonarsource DOT com
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package org.sonar.samples.java.checks;

import org.junit.jupiter.api.Test;
import org.sonar.java.checks.verifier.CheckVerifier;

class ZUseInterfacesRuleTest {

    @Test
    void verify() {
        CheckVerifier.newVerifier().onFile("src/test/files/ZUseInterfaces.java").withCheck(new ZUseInterfacesRule())
                .verifyIssues();
    }

    @Test
    void reduced() {
        CheckVerifier.newVerifier().onFile("src/test/files/ZUseInterfacesReduced.java").withCheck(new ZUseInterfacesRule())
                .verifyIssues();
    }

//  @Test
//  void testFile() {
//    CheckVerifier.newVerifier()
//      .onFile("/Users/rsalvador/gitcore/core-public/core/util/java/src/shared/common/api/AppVersion.java")
//      .withCheck(new ZUseInterfacesRule())
//      .verifyNoIssues();
//  }
}
