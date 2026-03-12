package com.amalitech.communityboard.events;

import com.amalitech.communityboard.models.Post;
import org.springframework.context.ApplicationEvent;

public class PostCreatedEvent extends ApplicationEvent {

    private final Post post;

    public PostCreatedEvent(Object source, Post post) {
        super(source);
        this.post = post;
    }

    public Post getPost() {
        return post;
    }
}

