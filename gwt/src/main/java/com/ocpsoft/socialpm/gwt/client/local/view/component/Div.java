package com.ocpsoft.socialpm.gwt.client.local.view.component;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

public class Div extends ComplexPanel
{
   public Div()
   {
      setElement((Element) Document.get().createDivElement().cast());
   }

   public Div(String s)
   {
      this();
      getElement().setInnerText(s);
   }

   public Div(Widget w)
   {
      this();
      super.add(w, getElement());
   }

   @Override
   public void add(Widget w)
   {
      super.add(w, getElement());
   }
   
   public void setStyle(String style)
   {
      getElement().setAttribute("style", style);
   }
}
