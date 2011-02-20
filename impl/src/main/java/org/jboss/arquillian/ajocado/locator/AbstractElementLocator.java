/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.jboss.arquillian.ajocado.locator;


/**
 * Abstract implementation of element locator able to derive attributes for itself.
 * 
 * @param <T>
 *            the type of locator which can be derived from this locator
 * 
 * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
 * @version $Revision$
 */
public abstract class AbstractElementLocator<T extends ElementLocator<T>> extends AbstractLocator<T> implements
    ElementLocator<T> {
    public AbstractElementLocator(String locator) {
        super(locator);
    }

    /**
     * Returns the default locator for attribute belongs to this locator.
     */
    public AttributeLocator<T> getAttribute(Attribute attribute) {
        return new DefaultAttributeLocator<T>(this, attribute);
    }
    
    @Override
    public ElementLocator<T> format(Object... args) {
        return (ElementLocator<T>) super.format(args);
    }
}
