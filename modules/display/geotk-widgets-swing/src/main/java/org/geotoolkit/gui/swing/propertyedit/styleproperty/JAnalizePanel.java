/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2009 Geomatys
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

package org.geotoolkit.gui.swing.propertyedit.styleproperty;

import java.awt.Color;

import java.awt.Component;
import java.text.NumberFormat;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.geotoolkit.gui.swing.resource.MessageBundle;
import org.geotoolkit.style.interval.IntervalStyleBuilder;
import org.geotoolkit.style.interval.IntervalStyleBuilder.METHOD;

import org.jdesktop.swingx.combobox.EnumComboBoxModel;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class JAnalizePanel extends javax.swing.JPanel {

    private static NumberFormat FORMAT = NumberFormat.getNumberInstance();

    private final IntervalStyleBuilder analyze;

    /** Creates new form JAnalizePanel */
    public JAnalizePanel(IntervalStyleBuilder analyze) {
        this.analyze = analyze;
        initComponents();


        guiMethod.setModel(new EnumComboBoxModel(IntervalStyleBuilder.METHOD.class));
        guiMethod.setRenderer(new MethodRenderer());

        refresh();
    }

    private void refresh(){
        guiCount.setText(String.valueOf(analyze.getCount()));
        guiSum.setText(FORMAT.format(analyze.getSum()));
        guiMinimum.setText(FORMAT.format(analyze.getMinimum()));
        guiMaximum.setText(FORMAT.format(analyze.getMaximum()));
        guiMean.setText(FORMAT.format(analyze.getMean()));
        guiMedian.setText(FORMAT.format(analyze.getMedian()));

        guiChart.removeAll();
        guiChart.add(new ChartPanel(getChart((Integer)guiDivision.getModel().getValue())));

        double[] values = analyze.getValues();
        Double[][] vals = new Double[values.length][1];

        for(int i=0; i<values.length; i++){
            vals[i][0] = values[i];
        }

        guiTable.setTableHeader(null);
        guiTable.setModel(new DefaultTableModel(vals, new String[]{""}){

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Double.class;
            }

        });
        guiTable.setShowGrid(false, false);
        guiTable.setHighlighters(new Highlighter[]{HighlighterFactory.createAlternateStriping(Color.white, HighlighterFactory.QUICKSILVER, 1)});
        guiTable.revalidate();
        guiTable.repaint();
        

        guiTable.setEditable(analyze.getMethod() == METHOD.MANUAL);


        guiTable.getModel().addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                double[] values = new double[guiTable.getModel().getRowCount()];
                for(int i=0;i<values.length;i++){
                    values[i] = Double.valueOf(guiTable.getModel().getValueAt(i, 0).toString());
                }

                analyze.setValues(values);

            }
        });


        guiMethod.setSelectedItem(analyze.getMethod());

    }



    public JFreeChart getChart(int nbDivision){
        XYSeries series = new XYSeries( "Data" ) ;

        double before = analyze.getMinimum();
        for(float i=1;i<=nbDivision;i++){
            final double localmin = before;
            final double localmax = analyze.getMinimum() + (i/nbDivision)*(analyze.getMaximum()-analyze.getMinimum());
            before = localmax;
            long localsum = 0;

            for(Double d : analyze.getAllValues()){
                if(i == 100){
                    //last element
                    if(d>=localmin && d<=localmax){
                        localsum++;
                    }
                }else{
                    if(d>=localmin && d<localmax){
                        localsum++;
                    }
                }
            }
            series.add( analyze.getMinimum()+(localmin+localmax)/2, localsum) ;
        }

	XYSeriesCollection dataset = new XYSeriesCollection(series);

        JFreeChart chart = ChartFactory.createXYBarChart(
		"",
		"",
		false,
		"",
		dataset,
		PlotOrientation.VERTICAL,
		false,
		false,
		false
		);

        XYPlot plot = chart.getXYPlot();
        ((XYBarRenderer)plot.getRenderer()).setShadowVisible(false);
        ((XYBarRenderer)plot.getRenderer()).setUseYInterval(false);
        ((XYBarRenderer)plot.getRenderer()).setMargin(0);

        chart.getPlot().setBackgroundAlpha(0);
        chart.setBackgroundPaint(new Color(0f,0f,0f,0f));

	return chart ;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        guiLblCount = new javax.swing.JLabel();
        guiLblMinimum = new javax.swing.JLabel();
        guiLblMaximum = new javax.swing.JLabel();
        guiLblSum = new javax.swing.JLabel();
        guiCount = new javax.swing.JLabel();
        guiMinimum = new javax.swing.JLabel();
        guiMaximum = new javax.swing.JLabel();
        guiSum = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        guiLblMean = new javax.swing.JLabel();
        guiLblMedian = new javax.swing.JLabel();
        guiMean = new javax.swing.JLabel();
        guiMedian = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        guiLblMethod = new javax.swing.JLabel();
        guiLblClasses = new javax.swing.JLabel();
        guiMethod = new javax.swing.JComboBox();
        guiClasses = new javax.swing.JSpinner();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        guiLblDivision = new javax.swing.JLabel();
        guiDivision = new javax.swing.JSpinner();
        guiChart = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        guiTable = new org.jdesktop.swingx.JXTable();

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        guiLblCount.setText(MessageBundle.getString("count")); // NOI18N

        guiLblMinimum.setText(MessageBundle.getString("minimum")); // NOI18N

        guiLblMaximum.setText(MessageBundle.getString("maximum")); // NOI18N

        guiLblSum.setText(MessageBundle.getString("sum")); // NOI18N

        guiCount.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        guiCount.setText("0");
        guiCount.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        guiMinimum.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        guiMinimum.setText("0");
        guiMinimum.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        guiMaximum.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        guiMaximum.setText("0");
        guiMaximum.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        guiSum.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        guiSum.setText("0");
        guiSum.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        guiLblMean.setText(MessageBundle.getString("mean")); // NOI18N

        guiLblMedian.setText(MessageBundle.getString("median")); // NOI18N

        guiMean.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        guiMean.setText("0");

        guiMedian.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        guiMedian.setText("0");

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);

        guiLblMethod.setText(MessageBundle.getString("method")); // NOI18N

        guiLblClasses.setText(MessageBundle.getString("classes")); // NOI18N

        guiMethod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiMethodActionPerformed(evt);
            }
        });

        guiClasses.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(5), Integer.valueOf(1), null, Integer.valueOf(1)));
        guiClasses.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                guiClassesStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(guiLblMethod)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiMethod, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(guiLblClasses)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiClasses, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(guiLblCount)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiCount))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(guiLblMean)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiMean))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(guiLblMedian)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiMedian)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(guiLblMinimum)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiMinimum))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(guiLblMaximum)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiMaximum))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(guiLblSum)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiSum)))
                .addContainerGap(103, Short.MAX_VALUE))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {guiClasses, guiMethod});

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {guiLblClasses, guiLblMethod});

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {guiLblCount, guiLblMean, guiLblMedian});

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {guiLblMaximum, guiLblMinimum, guiLblSum});

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {guiMaximum, guiMinimum, guiSum});

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {guiCount, guiMean, guiMedian});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(guiLblMethod)
                            .addComponent(guiMethod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(guiLblClasses)
                            .addComponent(guiClasses, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(guiLblCount)
                            .addComponent(guiCount))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(guiLblMean)
                            .addComponent(guiMean))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(guiLblMedian)
                            .addComponent(guiMedian)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(guiLblMinimum)
                            .addComponent(guiMinimum))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(guiLblMaximum)
                            .addComponent(guiMaximum))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(guiLblSum)
                            .addComponent(guiSum))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jSeparator3, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
            .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
        );

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(225);
        jSplitPane1.setDividerSize(3);

        jPanel1.setLayout(new java.awt.BorderLayout());

        guiLblDivision.setText(MessageBundle.getString("division")); // NOI18N

        guiDivision.setModel(new javax.swing.SpinnerNumberModel(50, 1, 100, 1));
        guiDivision.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                guiDivisionStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(guiLblDivision)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(guiDivision, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(138, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(guiLblDivision)
                .addComponent(guiDivision, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanel3, java.awt.BorderLayout.NORTH);

        guiChart.setLayout(new java.awt.BorderLayout());
        jPanel1.add(guiChart, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(jPanel1);

        guiTable.setBorder(null);
        guiTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(guiTable);

        jSplitPane1.setLeftComponent(jScrollPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void guiMethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiMethodActionPerformed
        analyze.setMethod((METHOD) guiMethod.getSelectedItem());
        refresh();
    }//GEN-LAST:event_guiMethodActionPerformed

    private void guiClassesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_guiClassesStateChanged
        analyze.setNbClasses((Integer)guiClasses.getModel().getValue());
        refresh();
    }//GEN-LAST:event_guiClassesStateChanged

    private void guiDivisionStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_guiDivisionStateChanged
        guiChart.removeAll();
        guiChart.add(new ChartPanel(getChart((Integer)guiDivision.getModel().getValue())));
        guiChart.revalidate();
        guiChart.repaint();
    }//GEN-LAST:event_guiDivisionStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel guiChart;
    private javax.swing.JSpinner guiClasses;
    private javax.swing.JLabel guiCount;
    private javax.swing.JSpinner guiDivision;
    private javax.swing.JLabel guiLblClasses;
    private javax.swing.JLabel guiLblCount;
    private javax.swing.JLabel guiLblDivision;
    private javax.swing.JLabel guiLblMaximum;
    private javax.swing.JLabel guiLblMean;
    private javax.swing.JLabel guiLblMedian;
    private javax.swing.JLabel guiLblMethod;
    private javax.swing.JLabel guiLblMinimum;
    private javax.swing.JLabel guiLblSum;
    private javax.swing.JLabel guiMaximum;
    private javax.swing.JLabel guiMean;
    private javax.swing.JLabel guiMedian;
    private javax.swing.JComboBox guiMethod;
    private javax.swing.JLabel guiMinimum;
    private javax.swing.JLabel guiSum;
    private org.jdesktop.swingx.JXTable guiTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSplitPane jSplitPane1;
    // End of variables declaration//GEN-END:variables


    private class MethodRenderer extends DefaultListCellRenderer{

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            MethodRenderer.this.setText(" ");

            if(value instanceof IntervalStyleBuilder.METHOD){
                METHOD mt = (METHOD) value;

                final String txt;
                switch(mt){
                    case EL : txt = MessageBundle.getString("el"); break;
                    case MANUAL : txt = MessageBundle.getString("manual"); break;
                    default : txt = MessageBundle.getString("qantile"); break;
                }
                MethodRenderer.this.setText(txt);
            }
            return MethodRenderer.this;
        }
    }


}
