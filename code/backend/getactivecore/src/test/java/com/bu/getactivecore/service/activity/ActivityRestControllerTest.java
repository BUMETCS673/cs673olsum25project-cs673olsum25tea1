package com.bu.getactivecore.service.activity;

import com.bu.getactivecore.model.activity.Activity;
import com.bu.getactivecore.service.activity.api.ActivityApi;
import com.bu.getactivecore.service.activity.entity.ActivityCreateRequestDto;
import com.bu.getactivecore.service.activity.entity.ActivityDto;
import com.bu.getactivecore.service.users.UsersController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
@WebAppConfiguration
class ActivityRestControllerTest {

        @Autowired
        private MockMvc m_mvc;

        @MockitoBean
        private ActivityService m_activityService;

        @Autowired
        private ActivityApi m_activityApi;

        @WithMockUser
        @Test
        void givenActivities_expectedActivitiesReturned() throws Exception {

                List<ActivityDto> mockedActivities = List.of(
                                ActivityDto.builder().name("Running").build(),
                                ActivityDto.builder().name("Yoga").build(),
                                ActivityDto.builder().name("Rock Climbing").build());

                Sort sort = Sort.by("id").ascending();
                Pageable pageable = PageRequest.of(0, 10, sort);
                Page<ActivityDto> page = new PageImpl<>(mockedActivities, pageable, mockedActivities.size());

                given(m_activityApi.getAllActivities(pageable)).willReturn(page);
                m_mvc.perform(
                                get("/v1/activities").accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("data.content[0].name").value("Running"))
                                .andExpect(jsonPath("data.content[1].name").value("Yoga"))
                                .andExpect(jsonPath("data.content[2].name").value("Rock Climbing"));
        }

        @WithMockUser
        @Test
        void givenNoActivities_then_200Returned() throws Exception {

                Page<ActivityDto> mockedActivities = Page.empty();
                Sort sort = Sort.by("id").ascending();
                Pageable pageable = PageRequest.of(0, 10, sort);
                given(m_activityApi.getAllActivities(pageable)).willReturn(mockedActivities);
                m_mvc.perform(
                                get("/v1/activities").accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("data.content").isEmpty());
        }

        @WithMockUser
        @Test
        void givenActivityFound_then_200Returned() throws Exception {

                Activity act1 = Activity.builder()
                                .name("Rock Climbing")
                                .startDateTime(LocalDateTime.now())
                                .location("Location")
                                .endDateTime(LocalDateTime.now())
                                .build();
                List<ActivityDto> mockedActivities = new ArrayList<>();
                mockedActivities.add(ActivityDto.of(act1));

                Sort sort = Sort.by("id").ascending();
                Pageable pageable = PageRequest.of(0, 10, sort);
                Page<ActivityDto> page = new PageImpl<>(mockedActivities, pageable, mockedActivities.size());

                given(m_activityApi.getActivityByName("Rock Climbing", pageable)).willReturn(page);
                m_mvc.perform(
                                get("/v1/activity/{name}", "Rock Climbing").accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("data.content[0].name").value("Rock Climbing"));
        }

        @WithMockUser
        @Test
        void givenActivityNotFound_then_200Returned() throws Exception {

                Page<ActivityDto> mockedActivities = Page.empty();
                Sort sort = Sort.by("id").ascending();
                Pageable pageable = PageRequest.of(0, 10, sort);
                given(m_activityApi.getActivityByName("Rock Climbing", pageable)).willReturn(mockedActivities);
                m_mvc.perform(
                                get("/v1/activity/{name}", "Rock Climbing")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON))

                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

}