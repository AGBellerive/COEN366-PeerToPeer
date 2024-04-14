package org.coen366;

public enum Status {
    //2.1 Registration and de Registration
    REGISTER,
    REGISTERED,
    REGISTER_DENIED,
    DE_REGISTER,

    //2.2 Publishing file related info
    PUBLISH,
    PUBLISHED,
    PUBLISH_DENIED,
    REMOVE,
    REMOVED,
    REMOVED_DENIED,

    //2.3 Server sharing information with the registered clients
    UPDATE,

    //2.4 File transfer between clients
    FILE_REQ,
    FILE_CONF,
    FILE,
    FILE_END,
    FILE_ERROR,

    //2.5 Clients updating their contact information
    UPDATE_CONTACT,
    UPDATE_CONFIRMED,
    UPDATE_DENIED,

}
