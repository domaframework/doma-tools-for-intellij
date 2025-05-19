/*
 * Copyright Doma Tools Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.domaframework.doma.intellij

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import org.domaframework.doma.intellij.extension.getResourcesSQLFile
import org.domaframework.doma.intellij.setting.SettingComponent
import org.domaframework.doma.intellij.setting.state.DomaToolsCustomFunctionSettings
import org.jetbrains.jps.model.java.JavaResourceRootType
import org.jetbrains.jps.model.java.JavaSourceRootType
import org.junit.Ignore
import java.io.File

@Ignore
open class DomaSqlTest : LightJavaCodeInsightFixtureTestCase() {
    protected val packagePath = "doma/example"
    private val resourceRoot = "main/resources"

    override fun getTestDataPath(): String = "src/test/testData/src/main/"

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        settingJdk()
        setDirectoryRoot()
        addLibrary("doma-core-3.2.0.jar", "doma-core")

        addEntityJavaFile("User.java")
        addEntityJavaFile("UserSummary.java")
        addEntityJavaFile("Employee.java")
        addEntityJavaFile("EmployeeSummary.java")
        addEntityJavaFile("Project.java")
        addEntityJavaFile("ProjectDetail.java")
        addEntityJavaFile("Principal.java")
    }

    @Throws(Exception::class)
    override fun tearDown() {
        try {
            deleteSdk()
        } finally {
            super.tearDown()
        }
    }

    /**
     * In GitAction's Test task, if there is no test case in this class,
     * a warning will be issued, so this is a workaround.
     */
    fun testDao() {}

    @Suppress("SameParameterValue")
    private fun addLibrary(
        jarName: String,
        libName: String,
    ) {
        val libPath = project.basePath + "/test/lib"
        val jarSource = File("src/test/lib/$jarName")
        val jarDes = File(libPath, jarName)
        FileUtil.copy(jarSource, jarDes)

        PsiTestUtil.addLibrary(
            testRootDisposable,
            myFixture.module,
            libName,
            libPath,
            jarName,
        )
    }

    private fun settingJdk() {
        deleteSdk()
        setUpJdk(myFixture.module)
    }

    private fun setUpJdk(module: Module) {
        val newJdk =
            JavaSdk.getInstance().createJdk("Doma Test JDK", System.getProperty("java.home"), false)

        WriteAction.runAndWait<RuntimeException> {
            ProjectJdkTable.getInstance().addJdk(newJdk)
            ModuleRootModificationUtil.updateModel(module) { model: ModifiableRootModel ->
                model.sdk = newJdk
            }
        }
    }

    private fun deleteSdk() {
        val oldJdk = ProjectJdkTable.getInstance().findJdk("Doma Test JDK")
        if (oldJdk == null) return
        WriteAction.runAndWait<RuntimeException> {
            ProjectJdkTable.getInstance().removeJdk(oldJdk)
            if (oldJdk is Disposable) {
                Disposer.dispose(oldJdk)
            }
        }
    }

    private fun setDirectoryRoot() {
        val mainDir = myFixture.tempDirFixture.findOrCreateDir("main")
        val javaDir = myFixture.tempDirFixture.findOrCreateDir("main/java")
        val resourcesDir = myFixture.tempDirFixture.findOrCreateDir(resourceRoot)
        WriteAction.runAndWait<RuntimeException> {
            ModuleRootModificationUtil.updateModel(myFixture.module) { model ->
                val contentEntry = model.addContentEntry(mainDir)
                contentEntry.addSourceFolder(javaDir, JavaSourceRootType.SOURCE)
                contentEntry.addSourceFolder(resourcesDir, JavaResourceRootType.RESOURCE)
            }
        }
    }

    fun addDaoJavaFile(vararg fileNames: String) {
        for (fileName in fileNames) {
            val file = File("$testDataPath/java/$packagePath/dao/$fileName")
            myFixture.addFileToProject(
                "main/java/$packagePath/dao/$fileName",
                file.readText(),
            )
        }
    }

    private fun addEntityJavaFile(fileName: String) {
        val file = File("$testDataPath/java/$packagePath/entity/$fileName")
        myFixture.addFileToProject(
            "main/java/$packagePath/entity/$fileName",
            file.readText(),
        )
    }

    fun addResourceEmptyFile(vararg sqlFileNames: String) {
        for (sqlFileName in sqlFileNames) {
            myFixture.addFileToProject(
                "$resourceRoot/META-INF/$packagePath/dao/$sqlFileName",
                "",
            )
        }
    }

    fun addSqlFile(vararg sqlNames: String) {
        for (sqlName in sqlNames) {
            val file = File("$testDataPath/resources/META-INF/$packagePath/dao/$sqlName")
            myFixture.addFileToProject(
                "$resourceRoot/META-INF/$packagePath/dao/$sqlName",
                file.readText(),
            )
        }
    }

    fun findSqlFile(sqlName: String): VirtualFile? {
        val module = myFixture.module
        return module?.getResourcesSQLFile(
            "$packagePath/dao/$sqlName",
            false,
        )
    }

    fun findDaoClass(testDaoName: String): PsiClass {
        val dao = myFixture.findClass("doma.example.dao.$testDaoName")
        assertNotNull("Not Found [$testDaoName]", dao)
        return dao
    }

    protected fun addExpressionJavaFile(fileName: String) {
        val file = File("$testDataPath/java/$packagePath/expression/$fileName")
        myFixture.addFileToProject(
            "main/java/$packagePath/entity/$fileName",
            file.readText(),
        )
    }

    protected fun settingCustomFunctions(newClassNames: MutableList<String>) {
        val settings = DomaToolsCustomFunctionSettings.getInstance(project)
        val component = SettingComponent()
        component.customFunctionClassNames = newClassNames.toMutableList()
        settings.apply(component)
        assertEquals(newClassNames, settings.getState().customFunctionClassNames)
    }
}
