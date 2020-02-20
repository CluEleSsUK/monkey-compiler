package cluelessuk

import cluelessuk.vm.MObject
import cluelessuk.vm.Null

class KotlinTestUtils {
    companion object {
        // can't use a sealed class concrete object directly in groovy
        @JvmStatic
        fun createNull(): MObject {
            return Null
        }
    }
}