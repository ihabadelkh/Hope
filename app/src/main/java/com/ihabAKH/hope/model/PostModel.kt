package com.ihabAKH.hope.model

class PostModel {
    var uid: String? = null
    var publisher: UserModel? = null
    var postText: String? = null
    var postImage: String? = null
    var likes: ArrayList<String>? = null
    var nLikes: Int? = null
    var comments: ArrayList<CommentModel>? = null
}