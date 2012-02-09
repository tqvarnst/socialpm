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
package com.ocpsoft.socialpm.gwt.server;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;

import org.jboss.seam.security.management.picketlink.IdentitySessionProducer;
import org.jboss.seam.transaction.TransactionPropagation;
import org.jboss.seam.transaction.Transactional;
import org.jboss.solder.servlet.WebApplication;
import org.jboss.solder.servlet.event.Initialized;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.IdentitySessionFactory;
import org.picketlink.idm.api.User;
import org.picketlink.idm.common.exception.IdentityException;

import com.ocpsoft.socialpm.model.config.Setting;
import com.ocpsoft.socialpm.model.project.Project;
import com.ocpsoft.socialpm.model.project.iteration.Iteration;
import com.ocpsoft.socialpm.model.security.IdentityObjectCredentialType;
import com.ocpsoft.socialpm.model.security.IdentityObjectType;
import com.ocpsoft.socialpm.model.user.Profile;
import com.ocpsoft.socialpm.services.project.ProjectService;

/**
 * Validates that the database contains the minimum required entities to function
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@Transactional(TransactionPropagation.REQUIRED)
public class InitializeDatabase
{
   @PersistenceContext(type = PersistenceContextType.EXTENDED)
   private EntityManager entityManager;

   @Inject
   private IdentitySessionFactory identitySessionFactory;

   @Transactional
   public void validate(@Observes @Initialized final WebApplication webapp) throws IdentityException
   {
      validateDB();
      validateIdentityObjectTypes();
      validateSecurity();
   }

   private void validateDB()
   {
      Setting singleResult = null;
      try {
         TypedQuery<Setting> query = entityManager.createQuery("from Setting s where s.name='schemaVersion'",
                  Setting.class);
         singleResult = query.getSingleResult();
      }
      catch (NoResultException e) {
         singleResult = new Setting("schemaVersion", "0");
         entityManager.persist(singleResult);
         entityManager.flush();
      }

      System.out.println("Current database schema version is [" + singleResult.getValue() + "]");

   }

   private void validateIdentityObjectTypes()
   {
      if (entityManager.createQuery("select t from IdentityObjectType t where t.name = :name")
               .setParameter("name", "USER")
               .getResultList().size() == 0) {

         IdentityObjectType user = new IdentityObjectType();
         user.setName("USER");
         entityManager.persist(user);
      }

      if (entityManager.createQuery("select t from IdentityObjectType t where t.name = :name")
               .setParameter("name", "GROUP")
               .getResultList().size() == 0) {

         IdentityObjectType group = new IdentityObjectType();
         group.setName("GROUP");
         entityManager.persist(group);
      }
   }

   private void validateSecurity() throws IdentityException
   {
      // Validate credential types
      if (entityManager.createQuery("select t from IdentityObjectCredentialType t where t.name = :name")
               .setParameter("name", "PASSWORD")
               .getResultList().size() == 0) {

         IdentityObjectCredentialType PASSWORD = new IdentityObjectCredentialType();
         PASSWORD.setName("PASSWORD");
         entityManager.persist(PASSWORD);
      }

      Map<String, Object> sessionOptions = new HashMap<String, Object>();
      sessionOptions.put(IdentitySessionProducer.SESSION_OPTION_ENTITY_MANAGER, entityManager);

      IdentitySession session = identitySessionFactory.createIdentitySession("default", sessionOptions);

      /*
       * Create our test user (me!)
       */
      if (session.getPersistenceManager().findUser("lincoln") == null) {
         User u = session.getPersistenceManager().createUser("lincoln");
         session.getAttributesManager().updatePassword(u, "password");
         session.getAttributesManager().addAttribute(u, "email", "lincoln@ocpsoft.com");

         Profile p = new Profile();
         p.setEmail("lincoln@ocpsoft.com");
         p.setUsername("lincoln");
         p.getIdentityKeys().add(u.getKey());
         p.setUsernameConfirmed(true);
         p.setShowBootcamp(true);
         entityManager.persist(p);

         Project project = new Project();
         project.setName("Social Project Management");
         project.setSlug("socialpm");

         ps.setEntityManager(entityManager);
         ps.create(p, project);

         Iteration i = new Iteration();
         i.setStartDate(new Date());
         i.setEndDate(new Date(System.currentTimeMillis() + (60 * 1000 * 60 * 24 * 2)));
         i.setProject(project);
         i.setTitle("Another");
         project.getIterations().add(i);

         i = new Iteration();
         i.setStartDate(new Date(System.currentTimeMillis() + (60 * 1000 * 60 * 24 * 3)));
         i.setEndDate(new Date(System.currentTimeMillis() + (+60 * 1000 * 60 * 24 * 10)));
         i.setProject(project);
         i.setTitle("Another2");
         project.getIterations().add(i);

         ps.save(project);
         entityManager.flush();
      }

      /*
       * Create test user (kenfinnigan)
       */
      if (session.getPersistenceManager().findUser("kenfinnigan") == null) {
         User u = session.getPersistenceManager().createUser("kenfinnigan");
         session.getAttributesManager().updatePassword(u, "password");
         session.getAttributesManager().addAttribute(u, "email", "ken@kenfinnigan.me");

         Profile p = new Profile();
         p.setEmail("ken@kenfinnigan.me");
         p.setUsername("kenfinnigan");
         p.getIdentityKeys().add(u.getKey());
         p.setUsernameConfirmed(true);
         p.setShowBootcamp(true);
         entityManager.persist(p);
         entityManager.flush();
      }

      /*
       * Create test user (bleathem)
       */
      if (session.getPersistenceManager().findUser("bleathem") == null) {
         User u = session.getPersistenceManager().createUser("bleathem");
         session.getAttributesManager().updatePassword(u, "password");
         session.getAttributesManager().addAttribute(u, "email", "bleathem@gmail.com");

         Profile p = new Profile();
         p.setEmail("bleathem@gmail.com");
         p.setUsername("bleathem");
         p.getIdentityKeys().add(u.getKey());
         p.setUsernameConfirmed(true);
         p.setShowBootcamp(true);
         entityManager.persist(p);
         entityManager.flush();
      }

   }

   @Inject
   private ProjectService ps;
}
