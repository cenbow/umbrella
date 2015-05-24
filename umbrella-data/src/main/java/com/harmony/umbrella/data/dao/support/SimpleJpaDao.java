/*
 * Copyright 2013-2015 wuxii@foxmail.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.harmony.umbrella.data.dao.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.harmony.umbrella.data.dao.JpaDao;
import com.harmony.umbrella.data.domain.Page;
import com.harmony.umbrella.data.domain.PageImpl;
import com.harmony.umbrella.data.domain.Pageable;
import com.harmony.umbrella.data.domain.Sort;
import com.harmony.umbrella.data.domain.Specification;
import com.harmony.umbrella.data.query.EntityInformation;
import com.harmony.umbrella.data.query.JpaEntityInformation;

/**
 * @author wuxii@foxmail.com
 */
public class SimpleJpaDao<E, ID extends Serializable> extends SimpleDao implements JpaDao<E, ID> {

	public static final String DELETE_ALL_QUERY_STRING = "delete from %s x";

	private EntityInformation<E, ID> ei;

	public SimpleJpaDao(Class<E> entityClass, EntityManager entityManager) {
		super(entityManager);
		this.ei = getEntityInformation(entityClass);
	}

	@Override
	public Page<E> findAll(Pageable pageable) {

		if (null == pageable) {
			return new PageImpl<E>(findAll());
		}

		return findAll(null, pageable);
	}

	@Override
	public Iterable<E> findAll(Sort sort) {
		return getQuery(null, sort).getResultList();
	}

	@Override
	public long count() {
		return countAll(ei.getJavaType());
	}

	@Override
	public void deleteAll() {
		deleteAll(ei.getJavaType());
	}

	@Override
	public E findOne(ID id) {
		return findOne(ei.getJavaType(), id);
	}

	@Override
	public List<E> findAll() {
		return findAll(ei.getJavaType());
	}

	@Override
	public List<E> findAll(Iterable<ID> ids) {
		if (ids == null || !ids.iterator().hasNext()) {
			return Collections.emptyList();
		}

		if (ei.hasCompositeId()) {

			List<E> results = new ArrayList<E>();

			for (ID id : ids) {
				results.add(findOne(id));
			}

			return results;
		}

		ByIdsSpecification<E> specification = new ByIdsSpecification<E>(ei);
		TypedQuery<E> query = getQuery(specification, (Sort) null);

		return query.setParameter(specification.parameter, ids).getResultList();
	}

	@Override
	public void flush() {
		em.flush();
	}

	@Override
	public E saveAndFlush(E entity) {

		E result = save(entity);
		flush();

		return result;
	}

	@Override
	public void deleteInBatch(Iterable<E> entities) {
		if (entities == null) {
			return;
		}
		if (!entities.iterator().hasNext()) {
			return;
		}

		StringBuilder buffer = new StringBuilder(String.format(DELETE_ALL_QUERY_STRING, ei.getEntityName()));

		buffer.append(" where");

		Iterator<E> iterator = entities.iterator();

		int i = 0;

		while (iterator.hasNext()) {

			iterator.next();

			buffer.append(String.format(" %s = ?%d", "x", ++i));

			if (iterator.hasNext()) {
				buffer.append(" or");
			}
		}

		Query query = em.createQuery(buffer.toString());

		i = 0;
		iterator = entities.iterator();

		while (iterator.hasNext()) {
			query.setParameter(++i, iterator.next());
		}

		query.executeUpdate();

	}

	@Override
	public void deleteAllInBatch() {
		deleteAll(ei.getJavaType());
	}

	@Override
	public E getOne(ID id) {
		return null;
	}

	@Override
	public E findOne(Specification<E> spec) {
		return null;
	}

	@Override
	public List<E> findAll(Specification<E> spec) {
		return null;
	}

	@Override
	public Page<E> findAll(Specification<E> spec, Pageable pageable) {
		return null;
	}

	@Override
	public List<E> findAll(Specification<E> spec, Sort sort) {
		return null;
	}

	@Override
	public long count(Specification<E> spec) {
		return 0;
	}

	protected TypedQuery<E> getQuery(Specification<E> spec, Sort sort) {
		return null;
	}

	protected EntityInformation<E, ID> getEntityInformation(Class<E> entityClass) {
		return new JpaEntityInformation<E, ID>(entityClass, em.getMetamodel());
	}

	private static final class ByIdsSpecification<T> implements Specification<T> {

		private final EntityInformation<T, ?> entityInformation;

		@SuppressWarnings("rawtypes")
		ParameterExpression<Iterable> parameter;

		public ByIdsSpecification(EntityInformation<T, ?> entityInformation) {
			this.entityInformation = entityInformation;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.jpa.domain.Specification#toPredicate(javax.persistence.criteria.Root, javax.persistence.criteria.CriteriaQuery, javax.persistence.criteria.CriteriaBuilder)
		 */
		public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

			Path<?> path = root.get(entityInformation.getIdAttribute());
			parameter = cb.parameter(Iterable.class);
			return path.in(parameter);
		}
	}
	//
	// @Override
	// public <T> T save(T entity) {
	// return dao.save(entity);
	// }
	//
	// @Override
	// public <T> Iterable<T> save(Iterable<T> entities) {
	// return dao.save(entities);
	// }
	//
	// @Override
	// public <T> T update(T entity) {
	// return dao.update(entity);
	// }
	//
	// @Override
	// public <T> Iterable<T> update(Iterable<T> entities) {
	// return dao.update(entities);
	// }
	//
	// @Override
	// public <T> T saveOrUpdate(T entity) {
	// return dao.saveOrUpdate(entity);
	// }
	//
	// @Override
	// public <T> Iterable<T> saveOrUpdate(Iterable<T> entities) {
	// return dao.saveOrUpdate(entities);
	// }
	//
	// @Override
	// public void delete(Object entity) {
	// dao.delete(entity);
	// }
	//
	// @Override
	// public <T> void delete(Iterable<T> entities) {
	// dao.delete(entities);
	// }
	//
	// @Override
	// public int deleteAll(Class<?> entityClass) {
	// return dao.deleteAll(entityClass);
	// }
	//
	// @Override
	// public <T> T delete(Class<T> entityClass, Serializable id) {
	// return dao.delete(entityClass, id);
	// }
	//
	// @Override
	// public <T> Iterable<T> delete(Class<T> entityClass, Iterable<? extends
	// Serializable> ids) {
	// return dao.delete(entityClass, ids);
	// }
	//
	// @Override
	// public <T> T findOne(Class<T> entityClass, Serializable id) {
	// return dao.findOne(entityClass, id);
	// }
	//
	// @Override
	// public <T> T findOne(String jpql) {
	// return dao.findOne(jpql);
	// }
	//
	// @Override
	// public <T> T findOne(String jpql, Object... parameters) {
	// return dao.findOne(jpql, parameters);
	// }
	//
	// @Override
	// public <T> T findOne(String jpql, Map<String, Object> parameters) {
	// return dao.findOne(jpql, parameters);
	// }
	//
	// @Override
	// public <T> T findOneBySQL(String sql, Class<T> resultClass) {
	// return dao.findOneBySQL(sql, resultClass);
	// }
	//
	// @Override
	// public <T> T findOneBySQL(String sql, Class<T> resultClass, Object...
	// parameters) {
	// return dao.findOneBySQL(sql, resultClass, parameters);
	// }
	//
	// @Override
	// public <T> T findOneBySQL(String sql, Class<T> resultClass, Map<String,
	// Object> parameters) {
	// return dao.findOneBySQL(sql, resultClass, parameters);
	// }
	//
	// @Override
	// public <T> List<T> findAll(Class<T> entityClass) {
	// return dao.findAll(entityClass);
	// }
	//
	// @Override
	// public <T> List<T> findAll(Class<T> entityClass, Sort sort) {
	// return dao.findAll(entityClass, sort);
	// }
	//
	// @Override
	// public <T> List<T> findAll(String jpql) {
	// return dao.findAll(jpql);
	// }
	//
	// @Override
	// public <T> List<T> findAll(String jpql, Object... parameters) {
	// return dao.findAll(jpql, parameters);
	// }
	//
	// @Override
	// public <T> List<T> findAll(String jpql, Map<String, Object> parameters) {
	// return dao.findAll(jpql, parameters);
	// }
	//
	// @Override
	// public <T> List<T> findAllBySQL(String sql, Class<T> resultClass) {
	// return dao.findAllBySQL(sql, resultClass);
	// }
	//
	// @Override
	// public <T> List<T> findAllBySQL(String sql, Class<T> resultClass,
	// Object... parameters) {
	// return dao.findAllBySQL(sql, resultClass, parameters);
	// }
	//
	// @Override
	// public <T> List<T> findAllBySQL(String sql, Class<T> resultClass,
	// Map<String, Object> parameters) {
	// return dao.findAllBySQL(sql, resultClass, parameters);
	// }
	//
	// @Override
	// public long countAll(Class<?> entityClass) {
	// return dao.countAll(entityClass);
	// }
	//
	// @Override
	// public long count(String jpql) {
	// return dao.count(jpql);
	// }
	//
	// @Override
	// public long count(String jpql, Object... parameters) {
	// return dao.count(jpql, parameters);
	// }
	//
	// @Override
	// public long count(String jpql, Map<String, Object> parameters) {
	// return dao.count(jpql, parameters);
	// }
	//
	// @Override
	// public long countBySQL(String sql) {
	// return dao.countBySQL(sql);
	// }
	//
	// @Override
	// public long countBySQL(String sql, Object... parameters) {
	// return dao.countBySQL(sql, parameters);
	// }
	//
	// @Override
	// public long countBySQL(String sql, Map<String, Object> parameters) {
	// return dao.countBySQL(sql, parameters);
	// }
	//
	// @Override
	// public int executeUpdate(String jpql) {
	// return dao.executeUpdate(jpql);
	// }
	//
	// @Override
	// public int executeUpdateBySQL(String sql) {
	// return dao.executeUpdateBySQL(sql);
	// }

}
