package com.unisport.dto;

import lombok.Data;

/**
 * Query params for "my invites" list.
 */
@Data
public class InviteMineQueryDTO {

    /**
     * Which list to view: host/joined/all.
     */
    private String view;

    /**
     * Status filter: open/full/finished/canceled/expired/all.
     */
    private String status;

    /**
     * Page number, starting from 1.
     */
    private Integer current;

    /**
     * Page size.
     */
    private Integer size;
}
