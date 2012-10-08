/**
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.arquillian.graphene.enricher;

import java.lang.reflect.Field;
import java.util.List;

import org.jboss.arquillian.graphene.context.GrapheneContext;
import org.jboss.arquillian.graphene.proxy.GrapheneProxy;
import org.jboss.arquillian.graphene.spi.annotations.Root;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.support.FindBy;

/**
 * Factory class for initializing the particular <b>Page Fragment</b>.
 * 
 * @author <a href="mailto:jhuska@redhat.com">Juraj Huska</a>
 * 
 */
public class Factory {

    /**
     * Returns initialized Page Fragment of given type. It means that all fields annotated with <code>@FindBy</code> and
     * <code>@Page</code> annotations are initialized properly.
     * 
     * @param clazzOfPageFragment the class of concrete Page Fragment implementation which will be initialized
     * @param rootOfPageFragment the root of the Page Fragment to reference its parts from it
     * @return properly initialized page fragment
     */
    public static <T> T initializePageFragment(Class<T> clazzOfPageFragment, final WebElement rootOfPageFragment) {
        if (rootOfPageFragment == null || clazzOfPageFragment == null) {
            throw new IllegalArgumentException("Non of the parameters can be null! Your parameters: " + clazzOfPageFragment
                + ", " + rootOfPageFragment);
        }

        T pageFragment = instantiatePageFragment(clazzOfPageFragment);

        List<Field> fields = ReflectionHelper.getFieldsWithAnnotation(clazzOfPageFragment, Root.class);
        int fieldSize = fields.size();
        if (fieldSize > 1) {
            throw new IllegalArgumentException("The Page Fragment " + pageFragment.getClass()
                + " can not have more than one field annotated with Root annotation! Your fields with @Root annotation: "
                + fields);
        }
        if (fieldSize == 1) {
            setObjectToField(fields.get(0), pageFragment, rootOfPageFragment);
        }

        fields = ReflectionHelper.getFieldsWithAnnotation(clazzOfPageFragment, FindBy.class);
        initNotPageFragmentsFields(fields, pageFragment, rootOfPageFragment);

        return pageFragment;
    }

    public static <T> T instantiatePageFragment(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            // TODO: handle exception
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * If the given root is null, the driver proxy is used for finding injected elements, otherwise the root element is used.
     * 
     * @param fields
     * @param object
     * @param root
     */
    public static void initNotPageFragmentsFields(List<Field> fields, Object object, WebElement root) {

        for (Field i : fields) {

            FindBy findBy = i.getAnnotation(FindBy.class);
            final By by = Factory.getReferencedBy(findBy);

            Class<?> fieldType = i.getType();

            if (fieldType.equals(WebElement.class)) {
                // it is plain WebElement field
                WebElement element = setUpTheProxyForWebElement(by, root);
                setObjectToField(i, object, element);

            } else if (fieldType.equals(List.class)) {
                // it is List of WebElements
                List<WebElement> elements = setUpTheProxyForListOfWebElements(by);
                setObjectToField(i, object, elements);
            }

        }
    }

    /**
     * Sets up the proxy element for the given By instance. If the given root is null, driver proxy is used for finding the web
     * element, otherwise the root element is used.
     * 
     * @param by
     * @param root
     * @return
     */
    public static WebElement setUpTheProxyForWebElement(final By by, final WebElement root) {
        // proxy for WebElement should implement also Locatable.class to be usable with org.openqa.selenium.interactions.Actions
        WebElement e = GrapheneProxy.getProxyForFutureTarget(new GrapheneProxy.FutureTarget() {

            @Override
            public Object getTarget() {
                WebDriver driver = GrapheneContext.getProxy();
                WebElement element = root == null ? driver.findElement(by) : root.findElement(by);
                return element;
            }
        }, WebElement.class, Locatable.class);
        return e;
    }

    public static void setObjectToField(Field field, Object objectWithField, Object object) {

        boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        try {
            field.set(objectWithField, object);
        } catch (Exception e) {
            // TODO more grained
            throw new RuntimeException("The given object" + object + " can not be set to the field " + field
                + " of the object which declares it: " + objectWithField + "!", e);
        }
        if (!accessible) {
            field.setAccessible(false);
        }
    }

    public static List<WebElement> setUpTheProxyForListOfWebElements(final By by) {
        List<WebElement> elements = GrapheneProxy.getProxyForFutureTarget(new GrapheneProxy.FutureTarget() {

            @Override
            public Object getTarget() {
                WebDriver driver = GrapheneContext.getProxy();
                List<WebElement> elements = driver.findElements(by);
                return elements;
            }
        }, List.class);
        return elements;
    }

    /*
     * can I do it in better way ?to iterate over all annotations methods and invoke them on what ?obviously it is not possible
     * to invoke it on annotation, since it can not be instantiated
     */
    public static By getReferencedBy(FindBy findByAnnotation) {
        String value = null;

        value = findByAnnotation.className().trim();
        if (!value.isEmpty()) {
            return By.className(value);
        }

        value = findByAnnotation.css().trim();
        if (!value.isEmpty()) {
            return By.cssSelector(value);
        }

        value = findByAnnotation.id().trim();
        if (!value.isEmpty()) {
            return By.id(value);
        }

        value = findByAnnotation.xpath().trim();
        if (!value.isEmpty()) {
            return By.xpath(value);
        }

        value = findByAnnotation.name().trim();
        if (!value.isEmpty()) {
            return By.name(value);
        }

        value = findByAnnotation.tagName().trim();
        if (!value.isEmpty()) {
            return By.tagName(value);
        }

        value = findByAnnotation.linkText().trim();
        if (!value.isEmpty()) {
            return By.linkText(value);
        }

        value = findByAnnotation.partialLinkText().trim();
        if (!value.isEmpty()) {
            return By.partialLinkText(value);
        }

        return null;
    }
}