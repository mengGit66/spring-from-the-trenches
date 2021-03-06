package net.petrikainulainen.spring.trenches.comment.controller;

import net.petrikainulainen.spring.trenches.UnitTestUtil;
import net.petrikainulainen.spring.trenches.comment.dto.CommentDTO;
import net.petrikainulainen.spring.trenches.config.ExampleApplicationContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.server.samples.context.WebContextLoader;

import javax.annotation.Resource;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

/**
 * @author Petri Kainulainen
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = WebContextLoader.class, classes = {ExampleApplicationContext.class})
//@ContextConfiguration(loader = WebContextLoader.class, locations = {"classpath:exampleApplicationContext.xml"})
@ActiveProfiles("webapp")
public class CommentControllerTest {

    private static final String COMMENT_TEXT = "comment";

    @Resource
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webApplicationContextSetup(webApplicationContext).build();
    }

    @Test
    public void add_CommentTextIsEmpty_ShouldReturnValidationError() throws Exception {
        CommentDTO added = new CommentDTO();
        mockMvc.perform(post("/api/comment")
                .contentType(UnitTestUtil.APPLICATION_JSON_UTF8)
                .body(UnitTestUtil.convertObjectToJsonBytes(added))
        )
                .andExpect(status().isBadRequest())
                .andExpect(content().mimeType(UnitTestUtil.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.fieldErrors", hasSize(1)))
                .andExpect(jsonPath("$.fieldErrors[0].field", is("text")))
                .andExpect(jsonPath("$.fieldErrors[0].message", is("Text cannot be empty.")));
    }

    @Test
    public void add_CommentTextIsTooLong_ShouldReturnValidationError() throws Exception {
        String added = UnitTestUtil.createStringWithLength(141);
        CommentDTO expected = createComment(added);
        mockMvc.perform(post("/api/comment")
                .contentType(UnitTestUtil.APPLICATION_JSON_UTF8)
                .body(UnitTestUtil.convertObjectToJsonBytes(expected))
        )
                .andExpect(status().isBadRequest())
                .andExpect(content().mimeType(UnitTestUtil.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.fieldErrors", hasSize(1)))
                .andExpect(jsonPath("$.fieldErrors[0].field", is("text")))
                .andExpect(jsonPath("$.fieldErrors[0].message", is("The maximum length of text is 140 characters.")));
    }

    @Test
    public void add_ValidInformationSet_ShouldReturnAddedComment() throws Exception {
        CommentDTO added = createComment(COMMENT_TEXT);

        mockMvc.perform(post("/api/comment")
                .contentType(UnitTestUtil.APPLICATION_JSON_UTF8)
                .body(UnitTestUtil.convertObjectToJsonBytes(added))
        )
                .andExpect(status().isOk())
                .andExpect(content().mimeType(UnitTestUtil.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.text", is(COMMENT_TEXT)));
    }

    private CommentDTO createComment(String text) {
        CommentDTO comment = new CommentDTO();
        comment.setText(text);
        return comment;
    }
}
