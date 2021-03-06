/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.service.persistence;

import com.liferay.portal.NoSuchBackgroundTaskException;
import com.liferay.portal.kernel.bean.PortalBeanLocatorUtil;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.test.ExecutionTestListeners;
import com.liferay.portal.kernel.util.IntegerWrapper;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.OrderByComparatorFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.model.BackgroundTask;
import com.liferay.portal.model.ModelListener;
import com.liferay.portal.service.BackgroundTaskLocalServiceUtil;
import com.liferay.portal.service.ServiceTestUtil;
import com.liferay.portal.service.persistence.BasePersistence;
import com.liferay.portal.service.persistence.PersistenceExecutionTestListener;
import com.liferay.portal.test.LiferayPersistenceIntegrationJUnitTestRunner;
import com.liferay.portal.test.persistence.TransactionalPersistenceAdvice;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import java.io.Serializable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Brian Wing Shun Chan
 */
@ExecutionTestListeners(listeners =  {
	PersistenceExecutionTestListener.class})
@RunWith(LiferayPersistenceIntegrationJUnitTestRunner.class)
public class BackgroundTaskPersistenceTest {
	@Before
	public void setUp() {
		_modelListeners = _persistence.getListeners();

		for (ModelListener<BackgroundTask> modelListener : _modelListeners) {
			_persistence.unregisterListener(modelListener);
		}
	}

	@After
	public void tearDown() throws Exception {
		Map<Serializable, BasePersistence<?>> basePersistences = _transactionalPersistenceAdvice.getBasePersistences();

		Set<Serializable> primaryKeys = basePersistences.keySet();

		for (Serializable primaryKey : primaryKeys) {
			BasePersistence<?> basePersistence = basePersistences.get(primaryKey);

			try {
				basePersistence.remove(primaryKey);
			}
			catch (Exception e) {
				if (_log.isDebugEnabled()) {
					_log.debug("The model with primary key " + primaryKey +
						" was already deleted");
				}
			}
		}

		_transactionalPersistenceAdvice.reset();

		for (ModelListener<BackgroundTask> modelListener : _modelListeners) {
			_persistence.registerListener(modelListener);
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = ServiceTestUtil.nextLong();

		BackgroundTask backgroundTask = _persistence.create(pk);

		Assert.assertNotNull(backgroundTask);

		Assert.assertEquals(backgroundTask.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		BackgroundTask newBackgroundTask = addBackgroundTask();

		_persistence.remove(newBackgroundTask);

		BackgroundTask existingBackgroundTask = _persistence.fetchByPrimaryKey(newBackgroundTask.getPrimaryKey());

		Assert.assertNull(existingBackgroundTask);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addBackgroundTask();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = ServiceTestUtil.nextLong();

		BackgroundTask newBackgroundTask = _persistence.create(pk);

		newBackgroundTask.setMvccVersion(ServiceTestUtil.nextLong());

		newBackgroundTask.setGroupId(ServiceTestUtil.nextLong());

		newBackgroundTask.setCompanyId(ServiceTestUtil.nextLong());

		newBackgroundTask.setUserId(ServiceTestUtil.nextLong());

		newBackgroundTask.setUserName(ServiceTestUtil.randomString());

		newBackgroundTask.setCreateDate(ServiceTestUtil.nextDate());

		newBackgroundTask.setModifiedDate(ServiceTestUtil.nextDate());

		newBackgroundTask.setName(ServiceTestUtil.randomString());

		newBackgroundTask.setServletContextNames(ServiceTestUtil.randomString());

		newBackgroundTask.setTaskExecutorClassName(ServiceTestUtil.randomString());

		newBackgroundTask.setTaskContext(ServiceTestUtil.randomString());

		newBackgroundTask.setCompleted(ServiceTestUtil.randomBoolean());

		newBackgroundTask.setCompletionDate(ServiceTestUtil.nextDate());

		newBackgroundTask.setStatus(ServiceTestUtil.nextInt());

		newBackgroundTask.setStatusMessage(ServiceTestUtil.randomString());

		_persistence.update(newBackgroundTask);

		BackgroundTask existingBackgroundTask = _persistence.findByPrimaryKey(newBackgroundTask.getPrimaryKey());

		Assert.assertEquals(existingBackgroundTask.getMvccVersion(),
			newBackgroundTask.getMvccVersion());
		Assert.assertEquals(existingBackgroundTask.getBackgroundTaskId(),
			newBackgroundTask.getBackgroundTaskId());
		Assert.assertEquals(existingBackgroundTask.getGroupId(),
			newBackgroundTask.getGroupId());
		Assert.assertEquals(existingBackgroundTask.getCompanyId(),
			newBackgroundTask.getCompanyId());
		Assert.assertEquals(existingBackgroundTask.getUserId(),
			newBackgroundTask.getUserId());
		Assert.assertEquals(existingBackgroundTask.getUserName(),
			newBackgroundTask.getUserName());
		Assert.assertEquals(Time.getShortTimestamp(
				existingBackgroundTask.getCreateDate()),
			Time.getShortTimestamp(newBackgroundTask.getCreateDate()));
		Assert.assertEquals(Time.getShortTimestamp(
				existingBackgroundTask.getModifiedDate()),
			Time.getShortTimestamp(newBackgroundTask.getModifiedDate()));
		Assert.assertEquals(existingBackgroundTask.getName(),
			newBackgroundTask.getName());
		Assert.assertEquals(existingBackgroundTask.getServletContextNames(),
			newBackgroundTask.getServletContextNames());
		Assert.assertEquals(existingBackgroundTask.getTaskExecutorClassName(),
			newBackgroundTask.getTaskExecutorClassName());
		Assert.assertEquals(existingBackgroundTask.getTaskContext(),
			newBackgroundTask.getTaskContext());
		Assert.assertEquals(existingBackgroundTask.getCompleted(),
			newBackgroundTask.getCompleted());
		Assert.assertEquals(Time.getShortTimestamp(
				existingBackgroundTask.getCompletionDate()),
			Time.getShortTimestamp(newBackgroundTask.getCompletionDate()));
		Assert.assertEquals(existingBackgroundTask.getStatus(),
			newBackgroundTask.getStatus());
		Assert.assertEquals(existingBackgroundTask.getStatusMessage(),
			newBackgroundTask.getStatusMessage());
	}

	@Test
	public void testCountByGroupId() {
		try {
			_persistence.countByGroupId(ServiceTestUtil.nextLong());

			_persistence.countByGroupId(0L);
		}
		catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCountByCompanyId() {
		try {
			_persistence.countByCompanyId(ServiceTestUtil.nextLong());

			_persistence.countByCompanyId(0L);
		}
		catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCountByStatus() {
		try {
			_persistence.countByStatus(ServiceTestUtil.nextInt());

			_persistence.countByStatus(0);
		}
		catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCountByG_T() {
		try {
			_persistence.countByG_T(ServiceTestUtil.nextLong(), StringPool.BLANK);

			_persistence.countByG_T(0L, StringPool.NULL);

			_persistence.countByG_T(0L, (String)null);
		}
		catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCountByG_TArrayable() {
		try {
			_persistence.countByG_T(ServiceTestUtil.nextLong(),
				new String[] {
					ServiceTestUtil.randomString(), StringPool.BLANK,
					StringPool.NULL, null, null
				});
		}
		catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCountByG_S() {
		try {
			_persistence.countByG_S(ServiceTestUtil.nextLong(),
				ServiceTestUtil.nextInt());

			_persistence.countByG_S(0L, 0);
		}
		catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCountByT_S() {
		try {
			_persistence.countByT_S(StringPool.BLANK, ServiceTestUtil.nextInt());

			_persistence.countByT_S(StringPool.NULL, 0);

			_persistence.countByT_S((String)null, 0);
		}
		catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCountByT_SArrayable() {
		try {
			_persistence.countByT_S(new String[] {
					ServiceTestUtil.randomString(), StringPool.BLANK,
					StringPool.NULL, null, null
				}, ServiceTestUtil.nextInt());
		}
		catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCountByG_N_T() {
		try {
			_persistence.countByG_N_T(ServiceTestUtil.nextLong(),
				StringPool.BLANK, StringPool.BLANK);

			_persistence.countByG_N_T(0L, StringPool.NULL, StringPool.NULL);

			_persistence.countByG_N_T(0L, (String)null, (String)null);
		}
		catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCountByG_T_C() {
		try {
			_persistence.countByG_T_C(ServiceTestUtil.nextLong(),
				StringPool.BLANK, ServiceTestUtil.randomBoolean());

			_persistence.countByG_T_C(0L, StringPool.NULL,
				ServiceTestUtil.randomBoolean());

			_persistence.countByG_T_C(0L, (String)null,
				ServiceTestUtil.randomBoolean());
		}
		catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCountByG_T_CArrayable() {
		try {
			_persistence.countByG_T_C(ServiceTestUtil.nextLong(),
				new String[] {
					ServiceTestUtil.randomString(), StringPool.BLANK,
					StringPool.NULL, null, null
				}, ServiceTestUtil.randomBoolean());
		}
		catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCountByG_T_S() {
		try {
			_persistence.countByG_T_S(ServiceTestUtil.nextLong(),
				StringPool.BLANK, ServiceTestUtil.nextInt());

			_persistence.countByG_T_S(0L, StringPool.NULL, 0);

			_persistence.countByG_T_S(0L, (String)null, 0);
		}
		catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCountByG_T_SArrayable() {
		try {
			_persistence.countByG_T_S(ServiceTestUtil.nextLong(),
				new String[] {
					ServiceTestUtil.randomString(), StringPool.BLANK,
					StringPool.NULL, null, null
				}, ServiceTestUtil.nextInt());
		}
		catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCountByG_N_T_C() {
		try {
			_persistence.countByG_N_T_C(ServiceTestUtil.nextLong(),
				StringPool.BLANK, StringPool.BLANK,
				ServiceTestUtil.randomBoolean());

			_persistence.countByG_N_T_C(0L, StringPool.NULL, StringPool.NULL,
				ServiceTestUtil.randomBoolean());

			_persistence.countByG_N_T_C(0L, (String)null, (String)null,
				ServiceTestUtil.randomBoolean());
		}
		catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		BackgroundTask newBackgroundTask = addBackgroundTask();

		BackgroundTask existingBackgroundTask = _persistence.findByPrimaryKey(newBackgroundTask.getPrimaryKey());

		Assert.assertEquals(existingBackgroundTask, newBackgroundTask);
	}

	@Test
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = ServiceTestUtil.nextLong();

		try {
			_persistence.findByPrimaryKey(pk);

			Assert.fail(
				"Missing entity did not throw NoSuchBackgroundTaskException");
		}
		catch (NoSuchBackgroundTaskException nsee) {
		}
	}

	@Test
	public void testFindAll() throws Exception {
		try {
			_persistence.findAll(QueryUtil.ALL_POS, QueryUtil.ALL_POS,
				getOrderByComparator());
		}
		catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	protected OrderByComparator getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create("BackgroundTask",
			"mvccVersion", true, "backgroundTaskId", true, "groupId", true,
			"companyId", true, "userId", true, "userName", true, "createDate",
			true, "modifiedDate", true, "name", true, "servletContextNames",
			true, "taskExecutorClassName", true, "taskContext", true,
			"completed", true, "completionDate", true, "status", true,
			"statusMessage", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		BackgroundTask newBackgroundTask = addBackgroundTask();

		BackgroundTask existingBackgroundTask = _persistence.fetchByPrimaryKey(newBackgroundTask.getPrimaryKey());

		Assert.assertEquals(existingBackgroundTask, newBackgroundTask);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = ServiceTestUtil.nextLong();

		BackgroundTask missingBackgroundTask = _persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingBackgroundTask);
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery = BackgroundTaskLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(new ActionableDynamicQuery.PerformActionMethod() {
				@Override
				public void performAction(Object object) {
					BackgroundTask backgroundTask = (BackgroundTask)object;

					Assert.assertNotNull(backgroundTask);

					count.increment();
				}
			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting()
		throws Exception {
		BackgroundTask newBackgroundTask = addBackgroundTask();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(BackgroundTask.class,
				BackgroundTask.class.getClassLoader());

		dynamicQuery.add(RestrictionsFactoryUtil.eq("backgroundTaskId",
				newBackgroundTask.getBackgroundTaskId()));

		List<BackgroundTask> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		BackgroundTask existingBackgroundTask = result.get(0);

		Assert.assertEquals(existingBackgroundTask, newBackgroundTask);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(BackgroundTask.class,
				BackgroundTask.class.getClassLoader());

		dynamicQuery.add(RestrictionsFactoryUtil.eq("backgroundTaskId",
				ServiceTestUtil.nextLong()));

		List<BackgroundTask> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting()
		throws Exception {
		BackgroundTask newBackgroundTask = addBackgroundTask();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(BackgroundTask.class,
				BackgroundTask.class.getClassLoader());

		dynamicQuery.setProjection(ProjectionFactoryUtil.property(
				"backgroundTaskId"));

		Object newBackgroundTaskId = newBackgroundTask.getBackgroundTaskId();

		dynamicQuery.add(RestrictionsFactoryUtil.in("backgroundTaskId",
				new Object[] { newBackgroundTaskId }));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingBackgroundTaskId = result.get(0);

		Assert.assertEquals(existingBackgroundTaskId, newBackgroundTaskId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(BackgroundTask.class,
				BackgroundTask.class.getClassLoader());

		dynamicQuery.setProjection(ProjectionFactoryUtil.property(
				"backgroundTaskId"));

		dynamicQuery.add(RestrictionsFactoryUtil.in("backgroundTaskId",
				new Object[] { ServiceTestUtil.nextLong() }));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	protected BackgroundTask addBackgroundTask() throws Exception {
		long pk = ServiceTestUtil.nextLong();

		BackgroundTask backgroundTask = _persistence.create(pk);

		backgroundTask.setMvccVersion(ServiceTestUtil.nextLong());

		backgroundTask.setGroupId(ServiceTestUtil.nextLong());

		backgroundTask.setCompanyId(ServiceTestUtil.nextLong());

		backgroundTask.setUserId(ServiceTestUtil.nextLong());

		backgroundTask.setUserName(ServiceTestUtil.randomString());

		backgroundTask.setCreateDate(ServiceTestUtil.nextDate());

		backgroundTask.setModifiedDate(ServiceTestUtil.nextDate());

		backgroundTask.setName(ServiceTestUtil.randomString());

		backgroundTask.setServletContextNames(ServiceTestUtil.randomString());

		backgroundTask.setTaskExecutorClassName(ServiceTestUtil.randomString());

		backgroundTask.setTaskContext(ServiceTestUtil.randomString());

		backgroundTask.setCompleted(ServiceTestUtil.randomBoolean());

		backgroundTask.setCompletionDate(ServiceTestUtil.nextDate());

		backgroundTask.setStatus(ServiceTestUtil.nextInt());

		backgroundTask.setStatusMessage(ServiceTestUtil.randomString());

		_persistence.update(backgroundTask);

		return backgroundTask;
	}

	private static Log _log = LogFactoryUtil.getLog(BackgroundTaskPersistenceTest.class);
	private ModelListener<BackgroundTask>[] _modelListeners;
	private BackgroundTaskPersistence _persistence = (BackgroundTaskPersistence)PortalBeanLocatorUtil.locate(BackgroundTaskPersistence.class.getName());
	private TransactionalPersistenceAdvice _transactionalPersistenceAdvice = (TransactionalPersistenceAdvice)PortalBeanLocatorUtil.locate(TransactionalPersistenceAdvice.class.getName());
}