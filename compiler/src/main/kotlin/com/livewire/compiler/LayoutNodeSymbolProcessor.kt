package com.livewire.compiler

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.livewire.annotations.LivewireLayoutSerializer
import com.livewire.annotations.LivewireSerializer
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

class LayoutNodeSymbolProcessor internal constructor(
  private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {

  class Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
      return LayoutNodeSymbolProcessor(environment)
    }
  }

  private val LayoutNodeClassName = ClassName("com.livewire.ui.layout", "LayoutNode")

  private val codeGenerator: CodeGenerator get() = environment.codeGenerator
  private val logger: KSPLogger get() = environment.logger
  private var generated = false

  override fun process(resolver: Resolver): List<KSAnnotated> {
    if (generated) return emptyList()

    // Scan all source files to find annotated declarations since KSP metadata
    // compilation may not fully resolve annotation types from KMP dependencies
    val serializerInterfaces = resolver
      .getSymbolsWithAnnotation(LivewireLayoutSerializer::class.qualifiedName!!)
      .filterIsInstance<KSClassDeclaration>()
      .toList()

    if (serializerInterfaces.isEmpty()) return emptyList()

    val layoutNodes = resolver
      .getSymbolsWithAnnotation(LivewireSerializer::class.qualifiedName!!)
      .filterIsInstance<KSClassDeclaration>()
      .filter { decl ->
        decl.getAllSuperTypes()
          .any { it.toClassName() == LayoutNodeClassName }
      }
      .toList()

    logger.warn("Found ${layoutNodes.size} layout nodes")

    // Defer if no layout nodes found yet (they may come in a later round)
    if (layoutNodes.isEmpty()) {
      return serializerInterfaces
    }

    for (serializerInterface in serializerInterfaces) {
      generateImpl(serializerInterface, layoutNodes)
    }

    generated = true
    return emptyList()
  }

  private fun generateImpl(
    serializerInterface: KSClassDeclaration,
    layoutNodes: List<KSClassDeclaration>,
  ) {
    val packageName = serializerInterface.packageName.asString()
    val className = serializerInterface.simpleName.asString()

    val serializersModuleType = ClassName("kotlinx.serialization.modules", "SerializersModule")

    val polymorphicMember = MemberName("kotlinx.serialization.modules", "polymorphic")
    val subclassMember = MemberName("kotlinx.serialization.modules", "subclass")
    val serializersModuleMember = MemberName("kotlinx.serialization.modules", "SerializersModule")

    val subclassBlock = CodeBlock.builder()
    subclassBlock.beginControlFlow("%M(%T::class)", polymorphicMember, LayoutNodeClassName)
    for (node in layoutNodes) {
      val nodeClass = node.toClassName()
      subclassBlock.addStatement("%M(%T::class, %T.serializer())", subclassMember, nodeClass, nodeClass)
    }
    subclassBlock.endControlFlow()

    val serializersModuleProperty = PropertySpec.builder("serializersModule", serializersModuleType)
      .addModifiers(KModifier.ACTUAL)
      .initializer(
        CodeBlock.builder()
          .beginControlFlow("%M", serializersModuleMember)
          .add(subclassBlock.build())
          .endControlFlow()
          .build(),
      )
      .build()

    val layoutSerializerAnnotation = AnnotationSpec.builder(
      ClassName("com.livewire.annotations", "LivewireLayoutSerializer"),
    ).build()

    val actualClass = TypeSpec.classBuilder(className)
      .addModifiers(KModifier.ACTUAL)
      .addAnnotation(layoutSerializerAnnotation)
      .addProperty(serializersModuleProperty)
      .build()

    val fileSpec = FileSpec.builder(packageName, className)
      .addType(actualClass)
      .build()

    val allFiles = layoutNodes.mapNotNull { it.containingFile } +
      listOfNotNull(serializerInterface.containingFile)

    fileSpec.writeTo(
      codeGenerator = codeGenerator,
      dependencies = Dependencies(aggregating = true, *allFiles.toTypedArray()),
    )
  }
}
