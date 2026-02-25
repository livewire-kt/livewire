package com.r0adkll.livewire.compiler

import com.fueledbycaffeine.autoservice.AutoService
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
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

private const val ANNOTATION_LAYOUT_SERIALIZER = "com.r0adkll.livewire.annotations.LivewireLayoutSerializer"
private const val ANNOTATION_LAYOUT_NODE = "com.r0adkll.livewire.annotations.LivewireLayoutNode"

class LayoutNodeSymbolProcessor internal constructor(
  private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {

  @AutoService
  class Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
      return LayoutNodeSymbolProcessor(environment)
    }
  }

  private val codeGenerator: CodeGenerator get() = environment.codeGenerator
  private val logger: KSPLogger get() = environment.logger
  private var generated = false

  private fun KSClassDeclaration.hasAnnotation(fqName: String): Boolean {
    val shortName = fqName.substringAfterLast(".")
    return annotations.any { annotation ->
      val resolved = annotation.annotationType.resolve().declaration.qualifiedName?.asString()
      // Match by fully-qualified name if resolved, otherwise fall back to short name
      resolved == fqName || (resolved == null && annotation.shortName.asString() == shortName)
    }
  }

  override fun process(resolver: Resolver): List<KSAnnotated> {
    if (generated) return emptyList()

    // Scan all source files to find annotated declarations since KSP metadata
    // compilation may not fully resolve annotation types from KMP dependencies
    val serializerInterfaces = resolver.getSymbolsWithAnnotation(ANNOTATION_LAYOUT_SERIALIZER)
      .filterIsInstance<KSClassDeclaration>()
      .toList()

    if (serializerInterfaces.isEmpty()) return emptyList()

    val layoutNodes = resolver.getSymbolsWithAnnotation(ANNOTATION_LAYOUT_NODE)
      .filterIsInstance<KSClassDeclaration>()
      .toList()

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

    // Find the base LayoutNode class from the first node's supertype
    val layoutNodeClass = layoutNodes.firstOrNull()?.let { node ->
      node.superTypes.firstOrNull()?.resolve()?.declaration as? KSClassDeclaration
    }?.toClassName() ?: run {
      logger.error("No LayoutNode supertype found on annotated classes")
      return
    }

    val polymorphicMember = MemberName("kotlinx.serialization.modules", "polymorphic")
    val subclassMember = MemberName("kotlinx.serialization.modules", "subclass")
    val serializersModuleMember = MemberName("kotlinx.serialization.modules", "SerializersModule")

    val subclassBlock = CodeBlock.builder()
    subclassBlock.beginControlFlow("%M(%T::class)", polymorphicMember, layoutNodeClass)
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
          .build()
      )
      .build()

    val layoutSerializerAnnotation = AnnotationSpec.builder(
      ClassName("com.r0adkll.livewire.annotations", "LivewireLayoutSerializer")
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
