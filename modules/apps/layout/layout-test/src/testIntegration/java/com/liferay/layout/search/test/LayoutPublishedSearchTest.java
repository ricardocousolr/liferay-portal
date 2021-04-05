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

package com.liferay.layout.search.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Ricardo Couso
 */
@DataGuard(scope = DataGuard.Scope.METHOD)
@RunWith(Arquillian.class)
public class LayoutPublishedSearchTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group, TestPropsValues.getUserId());

		ServiceContextThreadLocal.pushServiceContext(_serviceContext);
	}

	@Test
	public void testPublishedPageSearch() throws PortalException {
		Layout layout = _addLayout();

		List<Layout> fooSearchLayouts = _layoutLocalService.getLayouts(
			_group.getGroupId(), "foo",
			new String[] {LayoutConstants.TYPE_CONTENT}, QueryUtil.ALL_POS,
			QueryUtil.ALL_POS, null);

		Assert.assertFalse(fooSearchLayouts.contains(layout));

		_publishLayout(layout);

		fooSearchLayouts = _layoutLocalService.getLayouts(
			_group.getGroupId(), "foo",
			new String[] {LayoutConstants.TYPE_CONTENT}, QueryUtil.ALL_POS,
			QueryUtil.ALL_POS, null);

		Assert.assertEquals(
			Collections.singletonList(layout), fooSearchLayouts);
	}

	private Layout _addLayout() throws PortalException {
		Indexer<Layout> indexer = IndexerRegistryUtil.getIndexer(Layout.class);

		Layout layout = _layoutLocalService.addLayout(
			TestPropsValues.getUserId(), _group.getGroupId(), false,
			LayoutConstants.DEFAULT_PARENT_LAYOUT_ID, "foo foo foo bar",
			RandomTestUtil.randomString(), StringPool.BLANK,
			LayoutConstants.TYPE_CONTENT, false, StringPool.BLANK,
			_serviceContext);

		indexer.reindex(layout);

		return layout;
	}

	private void _publishLayout(Layout layout) throws PortalException {
		ReflectionTestUtil.invoke(
			_mvcActionCommand, "_publishLayout",
			new Class<?>[] {
				Layout.class, Layout.class, ServiceContext.class, long.class
			},
			layout.fetchDraftLayout(), layout, _serviceContext,
			TestPropsValues.getUserId());
	}

	private Group _group;

	@Inject
	private LayoutLocalService _layoutLocalService;

	@Inject(
		filter = "mvc.command.name=/layout_content_page_editor/publish_layout"
	)
	private MVCActionCommand _mvcActionCommand;

	private ServiceContext _serviceContext;

}