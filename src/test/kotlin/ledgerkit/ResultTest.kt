package ledgerkit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ledgerkit.util.Result
import kotlin.test.assertFailsWith
import ledgerkit.util.Result.Ok
import ledgerkit.util.Result.Err
import ledgerkit.util.err
import ledgerkit.util.ok
import ledgerkit.util.getOrThrow
import ledgerkit.util.map
import ledgerkit.util.onErr
import ledgerkit.util.onOk

class ResultTest {
    @Test
    fun mapPropagates() {
        val mapped = ok(2).map { it * 2 }
        assertEquals(4, (mapped as Ok).value)
    }

    @Test
    fun mapKeepsErr() {
        val errResult: Result<Int> = err("nope")
        val mapped = errResult.map { it * 2 }
        assertTrue(mapped is Err)
    }

    @Test
    fun onOkAndOnErrCallbacks() {
        var okCalled = false
        var errCalled = false
        ok(1).onOk { okCalled = true }.onErr { _, _ -> errCalled = true }
        err("fail").onErr { _, _ -> errCalled = true }.onOk { okCalled = true }
        assertTrue(okCalled)
        assertTrue(errCalled)
    }

    @Test
    fun getOrThrowKeepsMessage() {
        val ex = assertFailsWith<IllegalStateException> { err("boom").getOrThrow() }
        assertEquals("boom", ex.message)
    }

    @Test
    fun getOrThrowReturnsValue() {
        val value = ok("hi").getOrThrow()
        assertEquals("hi", value)
    }
}
