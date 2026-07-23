package org.wizkit.intellij.runner

import com.intellij.execution.lineMarker.RunLineMarkerContributor.Info
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * The contributor should put a run gutter icon on the @wizkit/testing registration
 * functions — suite/test and their describe/context/it aliases — whether they are
 * called directly or through the .only/.skip modifiers.
 */
class WizKitRunLineMarkerContributorTest : BasePlatformTestCase() {

    fun `test marks suite and test calls`() {
        assertHasRunMarker("""suite("app", () => {})""")
        assertHasRunMarker("""test("does something", () => {})""")
    }

    fun `test marks describe, context and it alias calls`() {
        assertHasRunMarker("""describe("app", () => {})""")
        assertHasRunMarker("""context("when idle", () => {})""")
        assertHasRunMarker("""it("does something", () => {})""")
    }

    fun `test marks only and skip modifier calls`() {
        assertHasRunMarker("""suite.only("just this suite", () => {})""")
        assertHasRunMarker("""suite.skip("not this suite", () => {})""")
        assertHasRunMarker("""test.only("just this test", () => {})""")
        assertHasRunMarker("""test.skip("not this test", () => {})""")
    }

    fun `test does not mark jasmine focused or excluded functions`() {
        assertNoRunMarker("""fdescribe("app", () => {})""")
        assertNoRunMarker("""xdescribe("app", () => {})""")
        assertNoRunMarker("""fit("does something", () => {})""")
        assertNoRunMarker("""xit("does something", () => {})""")
    }

    fun `test does not mark other member calls on test`() {
        assertNoRunMarker("""test.each("cases", () => {})""")
    }

    fun `test does not mark references that are not calls`() {
        assertNoRunMarker("""test""")
        assertNoRunMarker("""const t = test;""", markerOn = "test")
    }

    fun `test does not mark non-javascript files`() {
        assertNull(infoAt("""test("does something", () => {})""", fileName = "App.spec.txt"))
    }

    private fun assertHasRunMarker(code: String) {
        assertNotNull("expected a run marker on: $code", infoAt(code))
    }

    private fun assertNoRunMarker(code: String, markerOn: String? = null) {
        assertNull("expected no run marker on: $code", infoAt(code, markerOn = markerOn))
    }

    /**
     * Parses [code] and returns the contributor's Info for the leaf element at
     * [markerOn] (the leading identifier by default) — where the gutter icon would go.
     */
    private fun infoAt(code: String, fileName: String = "App.spec.js", markerOn: String? = null): Info? {
        val file = myFixture.configureByText(fileName, code)
        val offset = if (markerOn != null) code.indexOf(markerOn) else 0
        val element = file.findElementAt(offset)!!
        return WizKitRunLineMarkerContributor().getInfo(element)
    }
}
