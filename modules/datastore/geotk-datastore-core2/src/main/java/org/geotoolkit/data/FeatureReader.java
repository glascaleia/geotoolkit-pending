/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 * 
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.data;

import java.io.IOException;
import java.util.NoSuchElementException;
import org.geotoolkit.data.session.ContentException;

import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

/**
 * The low-level interface for reading Features. Will use the underlying
 * AttributeReader and the given FeatureType to create new Features.
 *
 * <p>
 * Typical use is as follows:
 * <pre><code>
 *   FeatureReader reader = null;
 *   try{
 *      for( reader = data.getFeatureReader( filter ); reader.hasNext(); ){
 *          f = reader.next();
 *          ...
 *      }
 *   }
 *   catch (IOException problem){
 *      ...
 *   }
 *   finally {
 *      if( reader != null ){
 *          try {
 *             reader.close();
 *          }
 *          catch( IOException eek){
 *          }
 *      }
 *   }
 * </code></pre>
 *
 * <h2>Questions and Suggestions</h2>
 * <ul>
 * <li>Q: Should FeatureReader provide access to the AttributeReaders it uses?
 * <br>A:
 *       No, it looks like we will make a lazy Feature in order to cleanly
 *       allow for lazy parsing of attribtues.
 * </li>
 * <li>Q:FeatureReader has a close method, but no open method?
 * <br>A: This is by design allowing FeatureReader to encapsulate its InputStream
 *     or Rowset). Please assume that FeatureReaders are a single use proposition.
 * </li>
 * <li>Q: All that exception handling is a pain!
 *     A:
 *        Yes it is, we have constructed semi-normal Java iterators to cut down on the
 *        pain. But you *do* still have to close 'em - this is IO after all.
 * </li>
 * <li>Q: Can we include skip(int) - SeanG
 *     A:
 *        The order of the contents is not "known" or predicatable to the end user, so
 *        skip( int ) would be useless. For random access (a higher order
 *        of abstraction then FeatureReader) please look at FeatureList.
 * </li>
 * </ul>
 * </p>
 *
 * @author Ian Schneider
 * @author Sean Geoghegan, Defence Science and Technology Organisation.
 * @version $Id$
 * @module pending
 */
public interface FeatureReader<T extends FeatureType, F extends Feature> extends FeatureIterator<F>{

    /**
     * Return the FeatureType this reader has been configured to create.
     *
     * @return the FeatureType of the Features this FeatureReader will create.
     */
    T getFeatureType();

    /**
     * Reads the next Feature in the FeatureReader.
     *
     * @return The next feature in the reader.
     *
     * @throws IOException If an error occurs reading the Feature.
     * @throws IllegalAttributeException If the attributes read do not comply
     *         with the FeatureType.
     * @throws NoSuchElementException If there are no more Features in the
     *         Reader.
     */
    @Override
    F next() throws ContentException;

    /**
     * Query whether this FeatureReader has another Feature.
     *
     * @return True if there are more Features to be read. In other words, true
     *         if calls to next would return a feature rather than throwing an
     *         exception.
     *
     * @throws IOException If an error occurs determining if there are more
     *         Features.
     */
    @Override
    boolean hasNext() throws ContentException;

}
