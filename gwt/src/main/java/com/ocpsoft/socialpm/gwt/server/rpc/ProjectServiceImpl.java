/**
 * This file is part of OCPsoft SocialPM: Agile Project Management Tools (SocialPM)
 *
 * Copyright (c)2011 Lincoln Baxter, III <lincoln@ocpsoft.com> (OCPsoft)
 * Copyright (c)2011 OCPsoft.com (http://ocpsoft.com)
 * 
 * If you are developing and distributing open source applications under
 * the GNU General Public License (GPL), then you are free to re-distribute SocialPM
 * under the terms of the GPL, as follows:
 *
 * SocialPM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SocialPM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SocialPM.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For individuals or entities who wish to use SocialPM privately, or
 * internally, the following terms do not apply:
 * 
 * For OEMs, ISVs, and VARs who wish to distribute SocialPM with their
 * products, or host their product online, OCPsoft provides flexible
 * OEM commercial licenses.
 * 
 * Optionally, Customers may choose a Commercial License. For additional
 * details, contact an OCPsoft representative (sales@ocpsoft.com)
 */
package com.ocpsoft.socialpm.gwt.server.rpc;

import java.util.List;

import javax.ejb.TransactionAttribute;
import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.jboss.errai.bus.server.annotations.Service;

import com.ocpsoft.common.util.Assert;
import com.ocpsoft.socialpm.gwt.client.shared.rpc.ProjectService;
import com.ocpsoft.socialpm.gwt.server.util.HibernateDetachUtility;
import com.ocpsoft.socialpm.gwt.server.util.HibernateDetachUtility.SerializationType;
import com.ocpsoft.socialpm.gwt.server.util.PersistenceUtil;
import com.ocpsoft.socialpm.model.feed.ProjectCreated;
import com.ocpsoft.socialpm.model.project.Feature;
import com.ocpsoft.socialpm.model.project.Project;
import com.ocpsoft.socialpm.model.project.iteration.Iteration;
import com.ocpsoft.socialpm.model.user.Profile;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RequestScoped
@Service
public class ProjectServiceImpl extends PersistenceUtil implements ProjectService
{
   private static final long serialVersionUID = 1403645951285144409L;

   @PersistenceContext(type = PersistenceContextType.EXTENDED)
   protected EntityManager em;

   @Override
   public EntityManager getEntityManager()
   {
      return em;
   }

   @TransactionAttribute
   public Project create(final Profile owner, final Project p)
   {
      p.setOwner(owner);
      super.create(p);

      Iteration unassigned = new Iteration();
      unassigned.setProject(p);
      unassigned.setTitle("Backlog");
      p.getIterations().add(unassigned);

      Feature bugFixes = new Feature();
      bugFixes.setName("Bug Fixes");
      bugFixes.setProject(p);
      p.getFeatures().add(bugFixes);
      bugFixes.setProject(p);

      Feature enhancements = new Feature();
      enhancements.setName("Enhancements");
      enhancements.setProject(p);
      p.getFeatures().add(enhancements);
      enhancements.setProject(p);

      Feature unclassified = new Feature();
      unclassified.setName("Unclassified");
      unclassified.setProject(p);
      p.getFeatures().add(unclassified);
      unclassified.setProject(p);

      super.create(p);

      // p.getMemberships().add(new Membership(p, owner, MemberRole.OWNER));

      super.create(new ProjectCreated(owner, p));
      return p;
   }

   public void save(final Project p)
   {
      super.save(p);
   }

   public long getProjectCount()
   {
      return count(Project.class);
   }

   public Project getByOwnerAndSlug(final Profile profile, final String slug)
   {
      Assert.notNull(profile, "Profile was null");
      Assert.notNull(slug, "Project slug was null");

      Project result = findUniqueByNamedQuery("project.byProfileAndSlug", profile, slug);
      HibernateDetachUtility.nullOutUninitializedFields(result, SerializationType.SERIALIZATION);
      return result;
   }

   @Override
   public List<Project> getByOwner(Profile profile)
   {
      Assert.notNull(profile, "Profile was null");
      Assert.notNull(profile.getUsername(), "Username was null");

      List<Project> list = findByNamedQuery("project.byProfileName", profile.getUsername());
      for (Project project : list) {
         em.detach(project);
         HibernateDetachUtility.nullOutUninitializedFields(project, SerializationType.SERIALIZATION);
      }
      System.out.println(list);
      return list;
   }
}
