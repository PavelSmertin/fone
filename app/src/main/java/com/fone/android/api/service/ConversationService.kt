package com.fone.android.api.service


interface ConversationService {


//    POST /api/conversations/{conversation_id}/messages
//    POST /api/conversations
//
//    POST /api/login
//    POST /api/refresh_token
//    GET /api/usernames?search={search}
//    GET /api/auth_user
//
//
//    GET /api/conversations?before={before}
//    GET /api/conversations/{conversation_id}
//    GET /api/conversations/{conversation_id}/messages?before={before}
//    GET /api/messages
//    GET /api/conversations/{conversation_id}/other_participant
//
//
//
//
//    @POST("conversations")
//    fun create(@Body request: ConversationRequest): Call<FoneResponse<ConversationResponse>>
//
//    @GET("conversations/{id}/messages")
//    fun getConversation(@Path("id") id: String): Call<FoneResponse<ConversationResponse>>
//
//    @GET("conversations/{id}/messages")
//    fun findConversation(@Path("id") id: String): Observable<FoneResponse<ConversationResponse>>
//
//    @POST("attachments")
//    fun requestAttachment(): Observable<FoneResponse<AttachmentResponse>>
//
//    @GET("attachments/{id}")
//    fun getAttachment(@Path("id") id: String): Call<FoneResponse<AttachmentResponse>>
//
//    @POST("conversations/{id}/participants/{action}")
//    fun participants(
//        @Path("id") id: String,
//        @Path("action") action: String,
//        @Body requests: List<ParticipantRequest>
//    ): Call<FoneResponse<ConversationResponse>>
//
//    @POST("conversations/{id}")
//    fun update(@Path("id") id: String, @Body request: ConversationRequest):
//            Call<FoneResponse<ConversationResponse>>
//
//    @POST("conversations/{id}")
//    fun updateAsync(@Path("id") id: String, @Body request: ConversationRequest):
//            Observable<FoneResponse<ConversationResponse>>
//
//    @POST("conversations/{id}/exit")
//    fun exit(@Path("id") id: String): Call<FoneResponse<ConversationResponse>>
//
//    @POST("conversations/{code_id}/join")
//    fun join(@Path("code_id") codeId: String): Observable<FoneResponse<ConversationResponse>>
//
//    @POST("conversations/{id}/rotate")
//    fun rotate(@Path("id") id: String): Observable<FoneResponse<ConversationResponse>>
//
//    @POST("conversations/{id}/mute")
//    fun mute(@Path("id") id: String, @Body request: ConversationRequest):
//            Call<FoneResponse<ConversationResponse>>
}