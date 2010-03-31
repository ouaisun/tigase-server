/*
 * Tigase Jabber/XMPP Server
 * Copyright (C) 2004-2007 "Artur Hefczyc" <artur.hefczyc@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 *
 * $Rev$
 * Last modified by $Author$
 * $Date$
 */

package tigase.db;

//~--- non-JDK imports --------------------------------------------------------

import static tigase.conf.Configurable.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

//~--- classes ----------------------------------------------------------------

/**
 * Describe class RepositoryFactory here.
 *
 *
 * Created: Tue Oct 24 22:13:52 2006
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */
public class RepositoryFactory {
	private static ConcurrentMap<String, ConcurrentMap<String, UserRepository>> user_repos =
		new ConcurrentHashMap<String, ConcurrentMap<String, UserRepository>>();
	private static ConcurrentMap<String, ConcurrentMap<String, UserAuthRepository>> auth_repos =
		new ConcurrentHashMap<String, ConcurrentMap<String, UserAuthRepository>>();

	//~--- get methods ----------------------------------------------------------

	/**
	 * Method description
	 *
	 *
	 * @param comp_name
	 * @param class_name
	 * @param resource
	 * @param params
	 *
	 * @return
	 *
	 * @throws ClassNotFoundException
	 * @throws DBInitException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static UserAuthRepository getAuthRepository(String comp_name, String class_name,
			String resource, Map<String, String> params)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			DBInitException {

		// XMLRepository is different as you can not have many instances accessing
		// the same file, thus we have to detect it and return always a hadle
		// to the same repository instance if it is accessing the same file
		if (class_name.equals("tigase.db.xml.XMLRepository")) {
			comp_name = resource;
		}

		ConcurrentMap<String, UserAuthRepository> repo_map = auth_repos.get(comp_name);

		if (repo_map == null) {
			repo_map = new ConcurrentHashMap<String, UserAuthRepository>();
			auth_repos.put(comp_name, repo_map);
		}    // end of if (repo_map == null)

		UserAuthRepository rep = repo_map.get(resource);

		if (rep == null) {
			rep = tryCastUserRepository(comp_name, resource);

			if ((rep != null) &&!rep.getClass().getName().equals(class_name)) {
				rep = null;
			}    // end of if (!rep.getClass().getName().equals(class_name))

			if (rep == null) {
				rep = (UserAuthRepository) Class.forName(getRepoClass(class_name)).newInstance();
				rep.initRepository(resource, params);
				repo_map.put(resource, rep);
			}    // end of if (rep == null)
		}      // end of if (rep == null)

		return rep;
	}

	/**
	 * Method description
	 *
	 *
	 * @param repo_name
	 *
	 * @return
	 */
	public static String getRepoClass(String repo_name) {
		String result = repo_name;

		if (repo_name.equals("mysql")) {
			result = MYSQL_REPO_CLASS_PROP_VAL;
		}

		if (repo_name.equals("pgsql")) {
			result = PGSQL_REPO_CLASS_PROP_VAL;
		}

		if (repo_name.equals("derby")) {
			result = DERBY_REPO_CLASS_PROP_VAL;
		}

		if (repo_name.equals("tigase-custom-auth") || repo_name.equals("tigase-custom")
				|| repo_name.equals("custom-auth")) {
			result = TIGASE_CUSTOM_AUTH_REPO_CLASS_PROP_VAL;
		}

		if (repo_name.equals("tigase-auth")) {
			result = TIGASE_AUTH_REPO_CLASS_PROP_VAL;
		}

		if (repo_name.equals("drupal") || repo_name.equals("wp")) {
			result = DRUPALWP_REPO_CLASS_PROP_VAL;
		}

		if (repo_name.equals("libresource")) {
			result = LIBRESOURCE_REPO_CLASS_PROP_VAL;
		}

		return result;
	}

	/**
	 * Method description
	 *
	 *
	 * @param comp_name
	 * @param class_name
	 * @param resource
	 * @param params
	 *
	 * @return
	 *
	 * @throws ClassNotFoundException
	 * @throws DBInitException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static UserRepository getUserRepository(String comp_name, String class_name,
			String resource, Map<String, String> params)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			DBInitException {

		// XMLRepository is different as you can not have many instances accessing
		// the same file, thus we have to detect it and return always a handle
		// to the same repository instance if it is accessing the same file
		if (class_name.equals("tigase.db.xml.XMLRepository")) {
			comp_name = resource;
		}

		ConcurrentMap<String, UserRepository> repo_map = user_repos.get(comp_name);

		if (repo_map == null) {
			repo_map = new ConcurrentHashMap<String, UserRepository>();
			user_repos.put(comp_name, repo_map);
		}    // end of if (repo_map == null)

		UserRepository rep = repo_map.get(resource);

		if (rep == null) {
			rep = (UserRepository) Class.forName(getRepoClass(class_name)).newInstance();
			rep.initRepository(resource, params);
			repo_map.put(resource, rep);
		}    // end of if (rep == null)

		return rep;
	}

	//~--- methods --------------------------------------------------------------

	private static UserAuthRepository tryCastUserRepository(String comp_name, String resource) {

		// There might be a repository class implementing both interfaces
		// it is always better access repositories through single instance
		// due to possible caching problems
		ConcurrentMap<String, UserRepository> repo_map = user_repos.get(comp_name);

		if (repo_map == null) {
			repo_map = new ConcurrentHashMap<String, UserRepository>();
			user_repos.put(comp_name, repo_map);
		}    // end of if (repo_map == null)

		UserRepository rep = repo_map.get(resource);

		if (rep != null) {
			try {
				return (UserAuthRepository) rep;
			} catch (Exception e) {}
		}

		return null;
	}
}    // RepositoryFactory


//~ Formatted in Sun Code Convention


//~ Formatted by Jindent --- http://www.jindent.com
