/*
 * Copyright (c) 2005-2010 Grameen Foundation USA
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 *  See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 *  explanation of the license and how it is applied.
 */

package org.mifos.application.questionnaire.migration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifos.application.master.business.CustomFieldDefinitionEntity;
import org.mifos.application.master.business.CustomFieldType;
import org.mifos.application.questionnaire.migration.mappers.QuestionnaireMigrationMapper;
import org.mifos.application.util.helpers.EntityType;
import org.mifos.application.util.helpers.YesNoFlag;
import org.mifos.customers.surveys.business.Survey;
import org.mifos.customers.surveys.business.SurveyInstance;
import org.mifos.customers.surveys.persistence.SurveysPersistence;
import org.mifos.customers.util.helpers.CustomerLevel;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.platform.questionnaire.builders.QuestionDtoBuilder;
import org.mifos.platform.questionnaire.builders.QuestionGroupDtoBuilder;
import org.mifos.platform.questionnaire.builders.QuestionGroupInstanceDtoBuilder;
import org.mifos.platform.questionnaire.builders.QuestionGroupResponseDtoBuilder;
import org.mifos.platform.questionnaire.builders.SectionDtoBuilder;
import org.mifos.platform.questionnaire.service.QuestionType;
import org.mifos.platform.questionnaire.service.QuestionnaireServiceFacade;
import org.mifos.platform.questionnaire.service.dtos.QuestionDto;
import org.mifos.platform.questionnaire.service.dtos.QuestionGroupDto;
import org.mifos.platform.questionnaire.service.dtos.QuestionGroupInstanceDto;
import org.mifos.platform.questionnaire.service.dtos.QuestionGroupResponseDto;
import org.mifos.platform.questionnaire.service.dtos.SectionDto;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Calendar;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mifos.customers.surveys.business.SurveyUtils.getSurvey;
import static org.mifos.customers.surveys.business.SurveyUtils.getSurveyInstance;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QuestionnaireMigrationTest {

    @Mock
    private QuestionnaireMigrationMapper questionnaireMigrationMapper;

    @Mock
    private QuestionnaireServiceFacade questionnaireServiceFacade;

    @Mock
    private SurveysPersistence surveysPersistence;

    private QuestionnaireMigration questionnaireMigration;

    private static final int QUESTION_GROUP_ID = 123;

    private Calendar calendar;

    @Before
    public void setUp() {
        questionnaireMigration = new QuestionnaireMigration(questionnaireMigrationMapper, questionnaireServiceFacade, surveysPersistence);
        calendar = Calendar.getInstance();
    }

    @Test
    public void shouldMigrateCustomFields() {
        List<CustomFieldDefinitionEntity> customFields = getCustomFields();
        QuestionGroupDto questionGroupDto = new QuestionGroupDto();
        when(questionnaireMigrationMapper.map(customFields)).thenReturn(questionGroupDto);
        when(questionnaireServiceFacade.createQuestionGroup(questionGroupDto)).thenReturn(QUESTION_GROUP_ID);
        Integer questionGroupId = questionnaireMigration.migrate(customFields);
        assertThat(questionGroupId, is(QUESTION_GROUP_ID));
        verify(questionnaireMigrationMapper).map(customFields);
        verify(questionnaireServiceFacade).createQuestionGroup(questionGroupDto);
    }

    @Test
    public void shouldMigrateSurveys() throws ApplicationException {
        Survey survey1 = getSurvey("Sur1", "Ques1", calendar.getTime());
        Survey survey2 = getSurvey("Sur2", "Ques2", calendar.getTime());
        QuestionGroupDto questionGroupDto1 = getQuestionGroupDto("Sur1", "Ques1", "View", "Client");
        QuestionGroupDto questionGroupDto2 = getQuestionGroupDto("Sur2", "Ques2", "View", "Client");
        SurveyInstance surveyInstance1 = getSurveyInstance(survey1, 12, 101, "Answer1");
        QuestionGroupInstanceDto questionGroupInstanceDto1 = getQuestionGroupInstanceDto("Answer1", 12, 101);
        SurveyInstance surveyInstance2 = getSurveyInstance(survey1, 13, 102, "Answer2");
        QuestionGroupInstanceDto questionGroupInstanceDto2 = getQuestionGroupInstanceDto("Answer2", 13, 102);
        SurveyInstance surveyInstance3 = getSurveyInstance(survey2, 12, 101, "Answer3");
        QuestionGroupInstanceDto questionGroupInstanceDto3 = getQuestionGroupInstanceDto("Answer3", 12, 101);
        SurveyInstance surveyInstance4 = getSurveyInstance(survey2, 13, 102, "Answer4");
        QuestionGroupInstanceDto questionGroupInstanceDto4 = getQuestionGroupInstanceDto("Answer4", 13, 102);
        when(questionnaireMigrationMapper.map(survey1)).thenReturn(questionGroupDto1);
        when(questionnaireMigrationMapper.map(survey2)).thenReturn(questionGroupDto2);
        when(questionnaireMigrationMapper.map(eq(surveyInstance1), anyInt())).thenReturn(questionGroupInstanceDto1);
        when(questionnaireMigrationMapper.map(eq(surveyInstance2), anyInt())).thenReturn(questionGroupInstanceDto2);
        when(questionnaireMigrationMapper.map(eq(surveyInstance3), anyInt())).thenReturn(questionGroupInstanceDto3);
        when(questionnaireMigrationMapper.map(eq(surveyInstance4), anyInt())).thenReturn(questionGroupInstanceDto4);
        when(questionnaireServiceFacade.createQuestionGroup(questionGroupDto1)).thenReturn(121);
        when(questionnaireServiceFacade.createQuestionGroup(questionGroupDto2)).thenReturn(122);
        when(questionnaireServiceFacade.saveQuestionGroupInstance(questionGroupInstanceDto1)).thenReturn(1111);
        when(questionnaireServiceFacade.saveQuestionGroupInstance(questionGroupInstanceDto2)).thenReturn(2222);
        when(questionnaireServiceFacade.saveQuestionGroupInstance(questionGroupInstanceDto3)).thenReturn(3333);
        when(questionnaireServiceFacade.saveQuestionGroupInstance(questionGroupInstanceDto4)).thenReturn(4444);
        when(surveysPersistence.retrieveInstancesBySurvey(survey1)).thenReturn(asList(surveyInstance1, surveyInstance2));
        when(surveysPersistence.retrieveInstancesBySurvey(survey2)).thenReturn(asList(surveyInstance3, surveyInstance4));
        List<Integer> questionGroupIds = questionnaireMigration.migrateSurveys(asList(survey1, survey2));
        assertThat(questionGroupIds, is(notNullValue()));
        assertThat(questionGroupIds.size(), is(2));
        assertThat(questionGroupIds.get(0), is(121));
        assertThat(questionGroupIds.get(1), is(122));
        verify(questionnaireMigrationMapper, times(2)).map(any(Survey.class));
        verify(questionnaireMigrationMapper, times(4)).map(any(SurveyInstance.class), anyInt());
        verify(questionnaireServiceFacade, times(2)).createQuestionGroup(any(QuestionGroupDto.class));
        verify(questionnaireServiceFacade, times(4)).saveQuestionGroupInstance(any(QuestionGroupInstanceDto.class));
        verify(surveysPersistence, times(2)).retrieveInstancesBySurvey(any(Survey.class));
    }

    private QuestionGroupDto getQuestionGroupDto(String questionGroupTitle, String questionTitle, String event, String source) {
        QuestionGroupDtoBuilder questionGroupDtoBuilder = new QuestionGroupDtoBuilder();
        QuestionDto questionDto = new QuestionDtoBuilder().withTitle(questionTitle).withType(QuestionType.FREETEXT).build();
        SectionDto sectionDto = new SectionDtoBuilder().withName("Misc").withOrder(0).withQuestions(asList(questionDto)).build();
        questionGroupDtoBuilder.withTitle(questionGroupTitle).withEventSource(event, source).withSections(asList(sectionDto));
        return questionGroupDtoBuilder.build();
    }

    private QuestionGroupInstanceDto getQuestionGroupInstanceDto(String response, Integer creatorId, Integer entityId) {
        QuestionGroupInstanceDtoBuilder instanceBuilder = new QuestionGroupInstanceDtoBuilder();
        QuestionGroupResponseDtoBuilder responseBuilder = new QuestionGroupResponseDtoBuilder();
        responseBuilder.withResponse(response).withSectionQuestion(999);
        QuestionGroupResponseDto questionGroupResponseDto = responseBuilder.build();
        instanceBuilder.withQuestionGroup(123).withCompleted(true).withCreator(creatorId).withEntity(entityId).withVersion(1).addResponses(questionGroupResponseDto);
        return instanceBuilder.build();
    }

    private List<CustomFieldDefinitionEntity> getCustomFields() {
        CustomFieldDefinitionEntity customField1 = new CustomFieldDefinitionEntity("CustomField1", CustomerLevel.CLIENT.getValue(),
               CustomFieldType.ALPHA_NUMERIC, EntityType.CLIENT, "Def1", YesNoFlag.YES);
        CustomFieldDefinitionEntity customField2 = new CustomFieldDefinitionEntity("CustomField2", CustomerLevel.CLIENT.getValue(),
               CustomFieldType.DATE, EntityType.CLIENT, "Def2", YesNoFlag.YES);
        CustomFieldDefinitionEntity customField3 = new CustomFieldDefinitionEntity("CustomField3", CustomerLevel.CLIENT.getValue(),
               CustomFieldType.NUMERIC, EntityType.CLIENT, "Def3", YesNoFlag.YES);
        return asList(customField1, customField2, customField3);
    }
}