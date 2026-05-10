<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Favorite;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class FavoriteController extends Controller
{
    /**
     * Display a listing of the resource.
     *
     * @return \Illuminate\Http\Response
     */
    public function index(Request $request)
    {
        $user = $request->user();
        $favorites = Favorite::where('user_id', $user->id)
            ->where('type', 'audio')
            ->get();

        return response()->json([
            'status' => 'success',
            'data' => $favorites
        ]);
    }

    /**
     * Store a newly created resource in storage.
     *
     * @param  \Illuminate\Http\Request  $request
     * @return \Illuminate\Http\Response
     */
    public function toggle(Request $request)
    {
        $request->validate([
            'post_id' => 'required|integer',
            'type' => 'string'
        ]);

        $user = $request->user();
        $postId = $request->post_id;
        $type = $request->type ?? 'audio';

        $favorite = Favorite::where('user_id', $user->id)
            ->where('post_id', $postId)
            ->where('type', $type)
            ->first();

        if ($favorite) {
            $favorite->delete();
            return response()->json([
                'status' => 'success',
                'message' => 'Removed from favorites',
                'is_favourite' => false
            ]);
        } else {
            Favorite::create([
                'user_id' => $user->id,
                'post_id' => $postId,
                'type' => $type
            ]);
            return response()->json([
                'status' => 'success',
                'message' => 'Added to favorites',
                'is_favourite' => true
            ]);
        }
    }

    /**
     * Check if a post is favorited.
     */
    public function check(Request $request)
    {
        $request->validate([
            'post_id' => 'required|integer',
            'type' => 'string'
        ]);

        $user = $request->user();
        $postId = $request->post_id;
        $type = $request->type ?? 'audio';

        $exists = Favorite::where('user_id', $user->id)
            ->where('post_id', $postId)
            ->where('type', $type)
            ->exists();

        return response()->json([
            'status' => 'success',
            'is_favourite' => $exists
        ]);
    }
}
