package info.archinnov.achilles;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import info.archinnov.achilles.annotations.ClusteringColumn;
import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.PartitionKey;
import info.archinnov.achilles.internal.metadata.parsing.EntityIntrospector;
import info.archinnov.achilles.internal.metadata.parsing.PropertyFilter;
import info.archinnov.achilles.internal.proxy.ProxyInterceptor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes("info.archinnov.achilles.annotations.Entity")
public class AchillesProcessor extends AbstractProcessor {

    private EntityIntrospector introspector = EntityIntrospector.Singleton.INSTANCE.get();
    private PropertyFilter filter = PropertyFilter.Singleton.INSTANCE.get();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Messager messager = processingEnv.getMessager();
        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                messager.printMessage(Diagnostic.Kind.NOTE,"********************* Found entity : " + element.getSimpleName());
                TypeElement proxyElt = (TypeElement) element;
                if (roundEnv.processingOver()) {
                    try {
                        generateProxy(proxyElt);
                    } catch (Exception e) {
                        messager.printMessage(Diagnostic.Kind.ERROR,e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }

    private void generateProxy(TypeElement proxyElt) throws Exception {
        final Class<?> entityClass = Class.forName(proxyElt.getQualifiedName().toString());
        String interceptorType = ProxyInterceptor.class.getName()+"<"+proxyElt.getSimpleName()+">";

        PackageElement pkg = processingEnv.getElementUtils().getPackageOf(proxyElt);
        JavaFileObject file = processingEnv.getFiler().createSourceFile(proxyElt.getQualifiedName() + "_AchillesSubClass", proxyElt);
        Writer writer = file.openWriter();
        writer.append("package ").append(pkg.getQualifiedName()).append(";\n");
        writer.append("public class ").append(proxyElt.getSimpleName()).append("_AchillesSubClass extends ").append(proxyElt.getQualifiedName()).append(" {\n");
        writer.append("private ").append(interceptorType).append(" interceptor;\n");
        writer.append("public void setInterceptor(").append(interceptorType).append(" interceptor) {\n");
        writer.append("this.interceptor = interceptor;\n");
        writer.append("}\n");

        List<Field> inheritedFields = introspector.getInheritedPrivateFields(entityClass);
        for (Field field : inheritedFields) {
            if(filter.hasAnnotation(field, PartitionKey.class) ||
                    filter.hasAnnotation(field, ClusteringColumn.class) ||
                    filter.hasAnnotation(field, Column.class)) {
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(), field.getDeclaringClass());
                final Method getter = pd.getReadMethod();
                final Method setter = pd.getWriteMethod();

                writer.append("public ").append(getter.getGenericReturnType().toString()).append(" ").append(getter.getName()).append("() {");
                writer.append(" //this.interceptor.interceptGetter()");
                writer.append(" return super.").append(getter.getName()).append("();");
                writer.append("}");

                writer.append("public ").append(setter.getGenericReturnType().toString()).append(" ").append(setter.getName()).append("(");
                writer.append(Joiner.on(",").join(setter.getGenericParameterTypes()));
                writer.append(") {");
                writer.append(" //this.interceptor.interceptSetter()");
                writer.append("}");

            }
        }
        writer.close();
    }

    private List<? extends VariableElement> printMethodParams(Writer writer, ExecutableElement methodElt) throws IOException {
        List<? extends VariableElement> parameters = methodElt.getParameters();
        for (int i = 0;i < parameters.size();i++) {
            VariableElement parameterElt = parameters.get(i);
            if (i > 0) {
                writer.append(",");
            }
            printType(parameterElt.asType(), writer);
            writer.append(" ");
            writer.append(parameterElt.getSimpleName());
        }
        return parameters;
    }

    private void printType(TypeMirror type, Appendable to) throws IOException {
        TypeElement typeElt = (TypeElement) ((DeclaredType) type).asElement();
        switch (type.getKind()) {
            case DECLARED:
                to.append(typeElt.getQualifiedName());
                break;
            default:
                to.append(typeElt.getSimpleName());
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
