/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotoolkit.process.coverage;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.geotoolkit.util.NumberRange;

/**
 *
 * @author sorel
 */
public class Boundary {

    private static final GeometryFactory GF = new GeometryFactory();
    private static final LinearRing[] EMPTY_RING_ARRAY = new LinearRing[0];

    //finished geometries
    private final List<LinearRing> holes = new ArrayList<LinearRing>();

    //in construction geometries
    private final LinkedList<LinkedList<Coordinate>> floatings = new LinkedList<LinkedList<Coordinate>>();
    public final NumberRange range;
    private int y = 0;

    public Boundary(NumberRange range){
        this.range = range;
    }

    public Boundary(NumberRange range, int y){
        this.range = range;
        this.y = y;
    }

    public void nextLine(){
        y++;
    }

    public void start(int firstX, int secondX){
        if(firstX == secondX) throw new IllegalArgumentException("bugging algorithm");
        
        final LinkedList ll = new LinkedList();
        ll.addFirst(new Coordinate(firstX, y));
        ll.addFirst(new Coordinate(firstX, y+1));
        ll.addLast(new Coordinate(secondX, y));
        ll.addLast(new Coordinate(secondX, y+1));
        floatings.add(ll);

        System.err.println("-----------------------------");
        for(int i=0;i<ll.size();i++){
            System.err.println(" - " + ll.get(i));
        }
    }

    //expend boundary
    public Polygon expend(int from, int to){

        LinkedList<Coordinate> fromList = null;
        boolean fromStart = false;

        LinkedList<Coordinate> toList = null;
        boolean toStart = false;

        for(final LinkedList<Coordinate> ll : floatings){

            if(fromList == null){
                final Coordinate first = ll.getFirst();
                final Coordinate last = ll.getLast();
                if(first.x == from){
                    fromStart = true;
                    fromList = ll;
                }else if(last.x == from){
                    fromStart = false;
                    fromList = ll;
                }
            }

            if(toList == null){
                final Coordinate first = ll.getFirst();
                final Coordinate last = ll.getLast();
                if(first.x == to){
                    toStart = true;
                    toList = ll;
                }else if(last.x == to){
                    toStart = false;
                    toList = ll;
                }
            }

            if(fromList != null && toList != null) break;
        }


        if(fromList != null && toList != null){
            if(fromList == toList){
                //same list finish it
                return finish(fromList);
            }else{
                return link(from, to);
            }

        }else if(fromList != null){
            if(fromStart) fromList.addFirst(new Coordinate(to, y));
            else          fromList.addLast(new Coordinate(to, y));
            return null;
        }else if(toList != null){
            if(toStart) toList.addFirst(new Coordinate(from, y));
            else        toList.addLast(new Coordinate(from, y));            
            return null;
        }

        throw new IllegalArgumentException("bugging algorithm");
    }

    //expand a coordinate list with a new point
    public void add(int lastX, int newX){

        System.err.println("Add : " + lastX + " New : " + newX );
        System.err.println("-> " + toString());


        for(final LinkedList<Coordinate> ll : floatings){
            final Coordinate first = ll.getFirst();
            if(first.x == lastX){
                if(newX == lastX){
                    //move the first point one unit down, avoid creating a new point at each line
                    first.y = y+1;
                }else{
                    ll.addFirst(new Coordinate(newX, y));
                    ll.addFirst(new Coordinate(newX, y+1));
                }
                return;
            }

            final Coordinate last = ll.getLast();
            if(last.x == lastX){
                if(newX == lastX){
                    //move the last point one unit down, avoid creating a new point at each line
                    last.y = y+1;
                }else{
                    ll.addLast(new Coordinate(newX, y));
                    ll.addLast(new Coordinate(newX, y+1));
                }
                return;
            }
        }

        throw new IllegalArgumentException("bugging algorithm");
    }

    //regroup two coordinate list
    public void gap(int firstX, int secondX){

        LinkedList<Coordinate> leftMost = null;
        LinkedList<Coordinate> rightMost = null;

        for(final LinkedList<Coordinate> ll : floatings){

            if(leftMost == null){
                final Coordinate last = ll.getLast();
                if(last.x == firstX){
                    leftMost = ll;
                    continue;
                }
            }

            if(rightMost == null){
                final Coordinate first = ll.getFirst();
                if(first.x == secondX){
                    rightMost = ll;
                    continue;
                }
            }

            if(leftMost != null && rightMost != null) break;
        }

        if(leftMost != null && rightMost != null){
            leftMost.addAll(rightMost);
            return;
        }

        throw new IllegalArgumentException("bugging algorithm");
    }

    public Polygon link(int firstX, int secondX){

        if(floatings.size() == 1){
            //first and last can only match first and last coord of the line
            final LinkedList<Coordinate> coords = floatings.get(0);
            final Coordinate first = coords.getFirst();
            final Coordinate last = coords.getLast();

            if(first.x == firstX && last.x == secondX){
                return finish(coords);
            }

            throw new IllegalArgumentException("bugging algorithm");
        }else{
            //first must be the last coord of a line
            //and last must be a start coord of another line

            LinkedList<Coordinate> leftMost = null;
            LinkedList<Coordinate> rightMost = null;

            for(final LinkedList<Coordinate> ll : floatings){
                final Coordinate first = ll.getFirst();
                final Coordinate last = ll.getLast();

                if(first.x == secondX){
                    rightMost = ll;
                }

                if(last.x == firstX){
                    leftMost = ll;
                }

                if(leftMost != null && rightMost != null) break;
            }

            if(leftMost != null && rightMost != null){
                if(leftMost == rightMost){
                    //same coordinate list, close this polygon
                    return finish(leftMost);
                }else{
                    //merge both list
                    for(final Coordinate c : rightMost){
                        leftMost.addLast(c);
                    }
                    floatings.remove(rightMost);
                    return null;
                }
            }

        }

        

        throw new IllegalArgumentException("bugging algorithm");
    }

    private Polygon finish(LinkedList<Coordinate> coords){

        if(floatings.size() == 1){
            //closing the polygon enveloppe
            //copy first point at the end
            coords.add(new Coordinate(coords.get(0)));
            final LinearRing exterior = GF.createLinearRing(coords.toArray(new Coordinate[coords.size()]));
            final Polygon polygon = GF.createPolygon(exterior, holes.toArray(EMPTY_RING_ARRAY));
            polygon.setUserData(range);
            return polygon;
        }else{
            //closing a hole in the geometry
            //copy first point at the end
            coords.add(new Coordinate(coords.get(0)));
            holes.add(GF.createLinearRing(coords.toArray(new Coordinate[coords.size()])));
            floatings.remove(coords);
            return null;
        }

    }

    public Polygon merge(Boundary candidate, int firstX, int secondX){

        //merge the floating sequences
        this.floatings.addAll(candidate.floatings);

        //merge the holes
        this.holes.addAll(candidate.holes);

        //link the floating
        Polygon poly = link(firstX, secondX);

        if(poly != null){
            //should not be possible at this state
            throw new IllegalArgumentException("bugging algorithm");
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Boundary : ");
        sb.append(range.toString());

        for(LinkedList<Coordinate> coords : floatings){
            sb.append("\n      line["+coords.getFirst()+","+coords.getLast()+"]");
        }

        return sb.toString();
    }

    public String toStringFull() {
        StringBuilder sb = new StringBuilder("Boundary : ");
        sb.append(range.toString());

        for(LinkedList<Coordinate> coords : floatings){
            sb.append("\n");
            for(Coordinate c : coords){
                sb.append("["+c.x+","+c.y+"]");
            }
        }

        return sb.toString();
    }

}
