/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009, Johann Sorel
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.display3d.geom;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.CullState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.geom.BufferUtils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.triangulate.ConformingDelaunayTriangulationBuilder;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Johann Sorel (Puzzle-GIS)
 * @module pending
 */
public class DefaultPolygonMesh extends Mesh {

    private static final double EPS = 1E-3;

    public DefaultPolygonMesh(Polygon geom, float minz, float maxz) {

        final ConformingDelaunayTriangulationBuilder builder = new ConformingDelaunayTriangulationBuilder();
        builder.setSites(geom);
        builder.setConstraints(geom);
        builder.setTolerance(EPS);
        GeometryCollection pieces = (GeometryCollection) builder.getTriangles(new GeometryFactory(geom.getPrecisionModel()));
        
        final List<Coordinate[]> triangles = new ArrayList<Coordinate[]>();
        for(int i=0,n=pieces.getNumGeometries();i<n;i++){
            final Polygon poly = (Polygon) pieces.getGeometryN(i);
            
            if(geom.contains(poly)){
                triangles.add(poly.getCoordinates());
            }

            //sometimes polygon arn't contain because of calculation number rounding
            if(geom.overlaps(poly)){
                double a = poly.getArea();
                double b = poly.intersection(geom).getArea();
                if(Math.abs(a - b) < EPS * Math.max(Math.abs(a), Math.abs(b))){
                    triangles.add(poly.getCoordinates());
                }
            }
        }

        //compress triangulation
        final List<Integer> indexes = new ArrayList<Integer>();
        final List<Coordinate> vertexes = new ArrayList<Coordinate>();
        compress(triangles, vertexes, indexes);
        final int nbTriangleVertex = vertexes.size();

        //find the facades
        final List<Coordinate[]> rings = new ArrayList<Coordinate[]>();
        final Coordinate[] exteriorRing = geom.getExteriorRing().getCoordinates();
        rings.add(exteriorRing);
        int nbQuadVertex = 4*(exteriorRing.length-1) ;

        for(int i=0,n=geom.getNumInteriorRing();i<n;i++){
            final Coordinate[] hole = geom.getInteriorRingN(i).getCoordinates();
            nbQuadVertex += 4*(hole.length-1);
            rings.add(hole);
        }

        final FloatBuffer vertexBuffer  = BufferUtils.createVector3Buffer(nbTriangleVertex+nbQuadVertex);
        final FloatBuffer normalBuffer  = BufferUtils.createVector3Buffer(nbTriangleVertex+nbQuadVertex);
        final IntBuffer indexBuffer     = BufferUtils.createIntBuffer(indexes.size()+nbQuadVertex);

        //make the facades
        int index = 0;
        for(Coordinate[] faces : rings){
            for(int i=0,n=faces.length-1;i<n;i++){
                Coordinate previous = faces[i];
                Coordinate coord = faces[i+1];

                double a = Math.PI/2;
                double x = previous.x - coord.x;
                double y = previous.y - coord.y;
                float nx = (float) (x * Math.cos(a) - y * Math.sin(a));
                float ny = (float) (x * Math.sin(a) + y * Math.cos(a));

                vertexBuffer.put((float)previous.x).put(maxz).put((float)previous.y);
                vertexBuffer.put((float)previous.x).put(minz).put((float)previous.y);
                vertexBuffer.put((float)coord.x).put(minz).put((float)coord.y);
                vertexBuffer.put((float)coord.x).put(maxz).put((float)coord.y);
                normalBuffer.put(nx).put(0).put(ny);
                normalBuffer.put(nx).put(0).put(ny);
                normalBuffer.put(nx).put(0).put(ny);
                normalBuffer.put(nx).put(0).put(ny);
                indexBuffer.put(index++).put(index++);
                indexBuffer.put(index++).put(index++);
            }
        }


        int start = index;

        //make the top face
        for(Coordinate c : vertexes){
            vertexBuffer.put((float)c.x).put(maxz).put((float)c.y);
            normalBuffer.put(0).put(1).put(0);
        }
        for(Integer i : indexes){
            indexBuffer.put(index+i);
        }

        //invert the triangles indexes
        for(int i=start;i<=indexBuffer.limit()-3;){
            int t1 = indexBuffer.get(i);
            int t2 = indexBuffer.get(i+1);
            int t3 = indexBuffer.get(i+2);

            indexBuffer.put(i, t3);
            indexBuffer.put(i+1, t2);
            indexBuffer.put(i+2, t1);

            i+=3;
        }


        _meshData.setVertexBuffer(vertexBuffer);
        _meshData.setNormalBuffer(normalBuffer);
        _meshData.setIndexBuffer(indexBuffer);
        _meshData.setIndexLengths(  new int[] {nbQuadVertex, indexes.size() } );
        _meshData.setIndexModes(    new IndexMode[] {IndexMode.Quads, IndexMode.Triangles } );

        final MaterialState ms = new MaterialState();
//        ms.setDiffuse(new ColorRGBA(0.5f, 0.7f, 0.5f, 0.8f)); //ColorRGBA.DARK_GRAY);
        ms.setDiffuse(ColorRGBA.GRAY);
        this.setRenderState(ms);

        final CullState cullFrontFace = new CullState();
        cullFrontFace.setEnabled(true);
        cullFrontFace.setCullFace(CullState.Face.Back);
        this.setRenderState(cullFrontFace);


        setModelBound(new BoundingBox());
        updateModelBound();
    }

    private static void compress(List<Coordinate[]> coords, List<Coordinate> vertexes, List<Integer> indexes){
        Map<Coordinate,Integer> lst = new HashMap<Coordinate,Integer>();
        
        int inc = -1;

        for(Coordinate[] coord : coords){
            for(int i=0;i<3;i++){
                //use only 3 first coords, the 4th one is the same as the first one
                Coordinate c = coord[i];
                Integer index = lst.get(c);
                if(index != null){
                    indexes.add(index);
                }else{
                    inc++;
                    vertexes.add(c);
                    indexes.add(inc);
                    lst.put(c, inc);
                }
            }
        }

    }

}
