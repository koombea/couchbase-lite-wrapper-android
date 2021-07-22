//
// Copyright (c) 2021 Koombea, Inc All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.koombea.couchbasewrapper.database

import com.couchbase.lite.PropertyExpression
import com.couchbase.lite.internal.utils.Preconditions

class CustomExpression {
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