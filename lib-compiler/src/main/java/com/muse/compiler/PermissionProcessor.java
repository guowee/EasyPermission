package com.muse.compiler;

import com.google.auto.service.AutoService;
import com.muse.annotation.PermissionFail;
import com.muse.annotation.PermissionSuccess;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Created by GuoWee on 2018/3/15.
 */
@AutoService(Processor.class)
public class PermissionProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Filer mFiler;
    private Messager mMessager;

    Map<String, ProxyInfo> classMap = new HashMap<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
        mMessager = processingEnv.getMessager();
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(PermissionSuccess.class.getCanonicalName());
        supportTypes.add(PermissionFail.class.getCanonicalName());
        return supportTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        classMap.clear();
        if (!gatherInformation(roundEnv, PermissionSuccess.class)) return false;
        if (!gatherInformation(roundEnv, PermissionFail.class)) return false;

        generateClassFile();
        return true;
    }


    private boolean gatherInformation(RoundEnvironment roundEnv, Class<? extends Annotation> clazz) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(clazz);
        for (Element element : elements) {

            if (!checkMethodValid(element, clazz)) return false;

            ExecutableElement annotatedMethod = (ExecutableElement) element;
            // class type
            TypeElement classElement = (TypeElement) annotatedMethod.getEnclosingElement();
            // class full name
            String classFullName = classElement.getQualifiedName().toString();

            ProxyInfo proxyInfo = classMap.get(classFullName);
            if (proxyInfo == null) {
                proxyInfo = new ProxyInfo(elementUtils, classElement);
                classMap.put(classFullName, proxyInfo);
                proxyInfo.setTypeElement(classElement);
            }

            Annotation annotation = annotatedMethod.getAnnotation(clazz);
            if (annotation instanceof PermissionSuccess) {
                int requestCode = ((PermissionSuccess) annotation).requestCode();
                proxyInfo.grantedMethodMap.put(requestCode, annotatedMethod.getSimpleName().toString());
            } else if (annotation instanceof PermissionFail) {
                int requestCode = ((PermissionFail) annotation).requestCode();
                proxyInfo.deniedMethodMap.put(requestCode, annotatedMethod.getSimpleName().toString());
            } else {
                error(element, "%s not support.", clazz.getSimpleName());
                return false;
            }
        }
        return true;
    }

    private void generateClassFile() {

        for (String key : classMap.keySet()) {
            ProxyInfo proxyInfo = classMap.get(key);
            try {
                mMessager.printMessage(Diagnostic.Kind.NOTE, "ClassFullName: " + proxyInfo.getProxyFullClassName());
                JavaFileObject object = mFiler.createSourceFile(proxyInfo.getProxyFullClassName(), proxyInfo.getTypeElement());

                Writer writer = object.openWriter();
                writer.write(proxyInfo.generateJavaCode());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                error(proxyInfo.getTypeElement(),
                        "Unable to write injector for type %s: %s",
                        proxyInfo.getTypeElement(), e.getMessage());
            }
        }

    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        mMessager.printMessage(Diagnostic.Kind.NOTE, message, element);
    }

    private boolean checkMethodValid(Element annotatedElement, Class clazz) {
        if (annotatedElement.getKind() != ElementKind.METHOD) {
            error(annotatedElement, "%s must be declared on method.", clazz.getSimpleName());
            return false;
        }
        if (ClassValidator.isPrivate(annotatedElement) || ClassValidator.isAbstract(annotatedElement)) {
            error(annotatedElement, "%s() must can not be abstract or private.", annotatedElement.getSimpleName());
            return false;
        }

        return true;
    }
}
