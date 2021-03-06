package com.ocpsoft.socialpm.gwt.client.local.view;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.ocpsoft.socialpm.gwt.client.local.EventsFactory;
import com.ocpsoft.socialpm.gwt.client.local.ServiceFactory;
import com.ocpsoft.socialpm.gwt.client.local.view.component.Div;
import com.ocpsoft.socialpm.gwt.client.local.view.component.HeroPanel;
import com.ocpsoft.socialpm.gwt.client.local.view.component.ListItem;
import com.ocpsoft.socialpm.gwt.client.local.view.component.ProjectList;
import com.ocpsoft.socialpm.gwt.client.local.view.component.Row;
import com.ocpsoft.socialpm.gwt.client.local.view.component.StatusFeed;
import com.ocpsoft.socialpm.gwt.client.local.view.component.UnorderedList;
import com.ocpsoft.socialpm.gwt.client.local.view.component.WelcomeBar;
import com.ocpsoft.socialpm.gwt.client.local.view.events.LoginEvent;
import com.ocpsoft.socialpm.gwt.client.shared.Response;
import com.ocpsoft.socialpm.model.feed.FeedEvent;
import com.ocpsoft.socialpm.model.project.Project;

@ApplicationScoped
public class HomeViewImpl extends FixedLayoutView implements HomeView
{
   private Presenter presenter;
   private final HeroPanel greeting = new HeroPanel();
   private final Anchor sendMessageButton = new Anchor("Send it »");
   private final TextBox messageBox = new TextBox();
   private final WelcomeBar welcomeBar = new WelcomeBar();
   private final Row dashboard = new Row();
   private final ProjectList projectList = new ProjectList();
   private final StatusFeed statusFeed = new StatusFeed();

   @Inject
   public HomeViewImpl(ServiceFactory serviceFactory, EventsFactory eventFactory)
   {
      super(serviceFactory, eventFactory);

      header.add(welcomeBar);
      welcomeBar.setVisible(false);
      
      dashboard.setVisible(false);      
      Div left = new Div();
      left.setStyleName("span6 cols");
      left.add(statusFeed);
      
      Div right = new Div();
      right.setStyleName("span6 cols");
      right.add(projectList);
      dashboard.add(left);
      dashboard.add(right);
      getContent().add(dashboard);

      System.out.println("Construct HomeView");
   }

   public void onLogin(@Observes LoginEvent event)
   {
      if (presenter != null)
         presenter.handleLogin(event);
   }

   public void response(@Observes Response event)
   {
      System.out.println("Observed response " + event.getMessage());
      greeting.setContent("Message from server: " + event.getMessage());
   }

   @Override
   public void setPresenter(HomeView.Presenter presenter)
   {
      this.presenter = presenter;
   }

   @Override
   public ComplexPanel getContent()
   {
      return content;
   }

   @Override
   public HeroPanel getGreeting()
   {
      return greeting;
   }

   @Override
   public TextBox getMessageBox()
   {
      return messageBox;
   }

   @Override
   public Anchor getSendMessageButton()
   {
      return sendMessageButton;
   }

   @Override
   public WelcomeBar getWelcomeBar()
   {
      return welcomeBar;
   }

   @Override
   public void showDashboard()
   {
      getGreeting().setVisible(false);
      dashboard.setVisible(true);
   }

   @Override
   public ProjectList getProjectList()
   {
      return projectList;
   }

}
