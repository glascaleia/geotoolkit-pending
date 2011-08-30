/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2011, Geomatys
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
package org.geotoolkit.mapfile.process;

import org.geotoolkit.mapfile.process.MapfileExpressionTokenizer.Token;
import java.util.List;
import java.util.ArrayList;

import org.geotoolkit.filter.DefaultFilterFactory2;
import org.geotoolkit.gui.swing.tree.Trees;
import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.style.DefaultStyleFactory;
import org.geotoolkit.style.MutableStyleFactory;

import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.FilterFactory;
import org.opengis.parameter.ParameterValueGroup;

import static org.geotoolkit.mapfile.process.MapfileFilterToOGCFilterDescriptor.*;
import static org.geotoolkit.parameter.Parameters.*;

/**
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class MapfileFilterToOGCFilterProcess extends AbstractProcess{
    
    private static final MutableStyleFactory SF = new DefaultStyleFactory();
    private static final FilterFactory FF = new DefaultFilterFactory2();
        
    public MapfileFilterToOGCFilterProcess(final ParameterValueGroup input){
        super(INSTANCE, input);
    }
    
    @Override
    public ParameterValueGroup call() throws ProcessException{
        
        String text  = value(IN_TEXT, inputParameters);
        final Expression ref  = value(IN_REFERENCE, inputParameters);
       
        
        final List<Token> tokens = MapfileExpressionTokenizer.toTokens(text);        
        System.out.println(Trees.toString("tokens", tokens));
        
        final Object result = parse(ref, tokens);
        getOrCreate(OUT_OGC, outputParameters).setValue(result);
        return outputParameters;
        
//        text = text.trim();
//        if(text.startsWith("\"") || text.startsWith("'")){
//            text = text.substring(1, text.length()-1);
//        }
//        
//        final Filter filter;
//        if(text.startsWith("/")){
//            //pattern match
//            text = text.substring(1, text.length()-1);
//            //TODO we handle only basic type for now
//            
//            final String[] parts = text.split("\\|");
//            if(parts.length == 1){
//                //Equal filter
//                filter = FF.equals(ref, FF.literal(text));
//            }else{
//                //several equal filter
//                final List<Filter> filters = new ArrayList<Filter>();
//                for(String part : parts){
//                    final Filter f = FF.equals(ref, FF.literal(part));
//                    filters.add(f);
//                }
//                filter = FF.or(filters);
//            }
//        }else{
//            filter = FF.equals(ref, FF.literal(text));
//        }
        
        
    }
    
    private static Object parse(final Expression ref,final List<Token> tokens){
        
        int index = 0;
        Object result = null;
        
        while(index < tokens.size()){
            Token token = tokens.get(index);
            
            if("(".equals(token.value)){
                //a block, find the block end ad parse it's content
                index++;
                final int blockEnd = getBlockEnd(tokens, index);
                final List<Token> content = tokens.subList(index, blockEnd);
                index = blockEnd+1;
                result = parse(ref, content);

            }else if("/".equals(token.value)){
                //a list of values
                final List<Filter> filters = new ArrayList<Filter>();
                while(true){
                    token = tokens.get(++index);
                    final Filter f = FF.equals(ref, FF.literal(token.value));
                    filters.add(f);
                    token = tokens.get(++index);
                    if("/".equals(token.value)){
                        break;
                    }
                }
                index++;
                result = FF.or(filters);

            }else if(token.value.startsWith("\"") || token.value.startsWith("\'")){
                index++;
                String text = token.value;
                if(text.startsWith("\"") || text.startsWith("'")){
                    text = text.substring(1, text.length()-1);
                }
                final Expression literal = toExpression(text);
                //a single value
                if(ref != null){
                    //it's a property equal to
                    result = FF.equals(ref,literal);
                }else{
                    //it's a literal
                    result = literal;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Parse string and replace encapsulated property names
     */
    private static Expression toExpression(String text){
        if(text.startsWith("\"") || text.startsWith("'")){
            text = text.substring(1, text.length()-1);
        }
        
        final List<Expression> parts = new ArrayList<Expression>();
        while(true){
            final int from = text.indexOf('[');
            final int end = text.indexOf(']');
            
            
            if(from >= 0){                
                if(from > 0){
                    final String before = text.substring(0, from);
                    parts.add(FF.literal(before));
                }
                
                final String propName = text.substring(from+1, end);
                parts.add(FF.property(propName));
                text = text.substring(end+1);
                
            }else if(text.length()>0 || parts.isEmpty()){
                parts.add(FF.literal(text));
                break;
            }else{
                break;
            }
        }
        
        //concatenate expressions
        while(parts.size()>1){
            final Expression exp1 = parts.remove(0);
            final Expression exp2 = parts.remove(0);
            final Expression concat = FF.function("strConcat", exp1, exp2);
            parts.add(0,concat);
        }
        
        return parts.get(0);
    }
    
    /**
     * @return int, token index of the closing current block
     */
    private static int getBlockEnd(final List<Token> tokens, int from){
        int block = 0;
        for(; from<tokens.size(); from++){
            final Token token = tokens.get(from);
            if("(".equals(token.value)){
                block++;
            }else if(")".equals(token.value)){
                block--;
                if(block<0){
                    //we found the end
                    return from;
                }
            }
        }
        
        return -1;
    }
    
}
