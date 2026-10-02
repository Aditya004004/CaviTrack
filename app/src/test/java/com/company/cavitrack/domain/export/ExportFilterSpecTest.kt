package com.company.cavitrack.domain.export

import com.company.cavitrack.domain.model.Component
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportFilterSpecTest {

    @Test
    fun defaultFilterSpec_hasNoActiveRestrictions() {
        val spec = ExportFilterSpec()
        assertEquals(null, spec.category)
        assertEquals(null, spec.startDate)
        assertEquals(null, spec.searchQuery)
    }

    @Test
    fun filterSpec_validatesCategoryAndQuantityMatching() {
        val spec = ExportFilterSpec(category = "Fasteners", minQty = 10, maxQty = 100)
        
        val comp1 = Component("1", "Bolt M8", "SKU1", "Fasteners", 50, "pcs", 5, "owner", null, 1000L, 1000L)
        val comp2 = Component("2", "Nut M8", "SKU2", "Fasteners", 5, "pcs", 2, "owner", null, 1000L, 1000L)
        val comp3 = Component("3", "Pipe 2inch", "SKU3", "Plumbing", 30, "pcs", 5, "owner", null, 1000L, 1000L)

        fun matches(c: Component): Boolean {
            if (spec.category != null && !c.category.equals(spec.category, ignoreCase = true)) return false
            if (spec.minQty != null && c.qty < spec.minQty!!) return false
            if (spec.maxQty != null && c.qty > spec.maxQty!!) return false
            return true
        }

        assertTrue(matches(comp1))
        assertFalse("Quantity too low", matches(comp2))
        assertFalse("Category mismatch", matches(comp3))
    }
}
