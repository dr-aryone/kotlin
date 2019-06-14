/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.jvm.lower.serialization.ir

import org.jetbrains.kotlin.backend.common.serialization.DescriptorUniqIdAware
import org.jetbrains.kotlin.backend.common.serialization.tryGetExtension
import org.jetbrains.kotlin.builtins.functions.FunctionClassDescriptor
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.load.java.descriptors.JavaClassConstructorDescriptor
import org.jetbrains.kotlin.load.java.descriptors.JavaClassDescriptor
import org.jetbrains.kotlin.load.java.descriptors.JavaMethodDescriptor
import org.jetbrains.kotlin.load.java.descriptors.JavaPropertyDescriptor
import org.jetbrains.kotlin.metadata.jvm.JvmProtoBuf
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassConstructorDescriptor
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedPropertyDescriptor
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedSimpleFunctionDescriptor

class JvmDescriptorUniqIdAware(val symbolTable: SymbolTable, val backoff: (IrSymbol) -> IrDeclaration) : DescriptorUniqIdAware {
    override fun DeclarationDescriptor.getUniqId(): Long? =
        when (this) {
            is DeserializedClassDescriptor -> this.classProto.tryGetExtension(JvmProtoBuf.classUniqId)?.index
                ?: referenceAndHash(this)
            is DeserializedSimpleFunctionDescriptor -> this.proto.tryGetExtension(JvmProtoBuf.functionUniqId)?.index
                ?: referenceAndHash(this)
            is DeserializedPropertyDescriptor -> this.proto.tryGetExtension(JvmProtoBuf.propertyUniqId)?.index
                ?: referenceAndHash(this)
            is DeserializedClassConstructorDescriptor -> this.proto.tryGetExtension(JvmProtoBuf.constructorUniqId)?.index
                ?: referenceAndHash(this)
            is JavaClassDescriptor,
            is JavaClassConstructorDescriptor,
            is JavaMethodDescriptor,
            is JavaPropertyDescriptor,
            is FunctionClassDescriptor -> referenceAndHash(this)
            else -> null
        }

    private fun referenceAndHash(descriptor: DeclarationDescriptor): Long? =
        if (descriptor is CallableMemberDescriptor && descriptor.kind === CallableMemberDescriptor.Kind.FAKE_OVERRIDE)
            null
        else with(JvmMangler) {
            referenceWithParents(descriptor).hashedMangle
        }


    private fun referenceWithParents(descriptor: DeclarationDescriptor): IrDeclaration {
        val original = descriptor.original
        val result = referenceOrDeclare(original)
        var currentDescriptor = original
        var current = result
        while (true) {
            val nextDescriptor = currentDescriptor.containingDeclaration!!
            if (nextDescriptor is PackageFragmentDescriptor) {
                current.parent = symbolTable.findOrDeclareExternalPackageFragment(nextDescriptor)
                break
            } else {
                val next = referenceOrDeclare(nextDescriptor)
                current.parent = next as IrDeclarationParent
                currentDescriptor = nextDescriptor
                current = next
            }
        }
        return result
    }

    private fun referenceOrDeclare(descriptor: DeclarationDescriptor): IrDeclaration =
        symbolTable.referenceMember(descriptor).also {
            if (!it.isBound) {
                backoff(it)
            }
        }.owner as IrDeclaration
}

fun newJvmDescriptorUniqId(index: Long): JvmProtoBuf.DescriptorUniqId =
    JvmProtoBuf.DescriptorUniqId.newBuilder().setIndex(index).build()

// May be needed in the future
//
//fun DeclarationDescriptor.willBeEliminatedInLowerings(): Boolean =
//        isAnnotationConstructor() ||
//                (this is PropertyAccessorDescriptor &&
//                        correspondingProperty.hasJvmFieldAnnotation())
