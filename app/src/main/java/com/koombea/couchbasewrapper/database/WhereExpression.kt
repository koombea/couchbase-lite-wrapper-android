package com.koombea.couchbasewrapper.database

import com.couchbase.lite.PropertyExpression
import com.couchbase.lite.internal.utils.Preconditions

class WhereExpression {
    companion object {
        /**
         * Use this static method to create a PropertyExpression instance with "attributes."
         * prepended to the property name. e.g: a where expression like:
         * Expression.property("attributes.id").equalTo(Expression.string("2"))
         * can be replaced by:
         * WhereExpression.property("id").equalTo(Expression.string("2"))
         * Although it is recommended to avoid using the second one as it uses reflection to access
         * the package-private constructor of PropertyExpression
         */
        fun property(property: String): PropertyExpression {
            Preconditions.assertNotNull(property, "property")
            val cl = Class.forName(PropertyExpression::class.java.name)
            val cons = cl.declaredConstructors.first {
                it.parameterTypes.size == 1 && it.parameterTypes[0].equals(java.lang.String::class.java)
            }
            cons.isAccessible = true
            return cons.newInstance("$ATTRIBUTES$property") as PropertyExpression
        }

        private const val ATTRIBUTES = "attributes."
    }
}