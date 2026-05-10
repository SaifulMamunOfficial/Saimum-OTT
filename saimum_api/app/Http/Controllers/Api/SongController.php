<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Audio;
use Illuminate\Http\JsonResponse;

class SongController extends Controller
{
    /**
     * Increment play/view count for a published track.
     *
     * POST /api/songs/{id}/increment-views
     */
    public function incrementViews(int $id): JsonResponse
    {
        $audio = Audio::query()
            ->whereKey($id)
            ->where('audio_status', '1')
            ->first();

        if ($audio === null) {
            return response()->json([
                'success' => '0',
                'message' => 'Song not found',
            ], 404);
        }

        $audio->increment('total_views');

        return response()->json([
            'success' => '1',
            'message' => 'View count updated',
            'total_views' => (int) $audio->fresh()->total_views,
        ]);
    }
}
