<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Audio;
use App\Models\Album;
use App\Models\Artist;
use Illuminate\Http\Request;

class HomeController extends Controller
{
    public function index()
    {
        $settings = \DB::table('tbl_settings')->first();
        $homeLimit = $settings->home_limit ?? 10;
        $musicLimit = $settings->music_limit ?? 20;

        $banners = \DB::table('tbl_banner')->where('status', '1')->get();
        
        // Trending and Recent for Home (use homeLimit)
        $trendingHome = Audio::where('audio_status', '1')->orderBy('total_views', 'desc')->limit($homeLimit)->get();
        $recentHome = Audio::where('audio_status', '1')->orderBy('id', 'desc')->limit($homeLimit)->get();
        
        // Trending and Recent for Music (use musicLimit)
        $trendingMusic = Audio::where('audio_status', '1')->orderBy('total_views', 'desc')->limit($musicLimit)->get();
        $recentMusic = Audio::where('audio_status', '1')->orderBy('id', 'desc')->limit($musicLimit)->get();

        $albums = Album::where('status', '1')->orderBy('aid', 'desc')->limit($musicLimit)->get();
        $artists = Artist::orderBy('id', 'desc')->limit($musicLimit)->get();

        return response()->json([
            'success' => '1',
            'banners' => $banners,
            'trending' => $trendingHome, // Default to home for backward compatibility or split later
            'recent' => $recentHome,
            'music_trending' => $trendingMusic,
            'music_recent' => $recentMusic,
            'albums' => $albums,
            'artists' => $artists,
            'settings' => [
                'home_limit' => $homeLimit,
                'music_limit' => $musicLimit
            ]
        ]);
    }

    public function albumDetails($id)
    {
        $album = Album::where('aid', $id)->first();
        if (!$album) return response()->json(['success' => '0', 'MSG' => 'Album not found']);

        $tracks = Audio::where('album_id', $id)->where('audio_status', '1')->orderBy('id', 'desc')->get();
        
        return response()->json([
            'success' => '1',
            'album' => $album,
            'tracks' => $tracks
        ]);
    }

    public function artistDetails($id)
    {
        $artist = Artist::where('id', $id)->first();
        if (!$artist) return response()->json(['success' => '0', 'MSG' => 'Artist not found']);

        // In legacy, it searched by artist name in the audio_artist comma-separated string
        $tracks = Audio::where('audio_artist', 'LIKE', "%{$artist->artist_name}%")
            ->where('audio_status', '1')
            ->orderBy('total_views', 'desc')
            ->get();

        $albums = Album::where('status', '1')
            ->whereRaw('FIND_IN_SET(?, artist_ids)', [$artist->id])
            ->orderBy('aid', 'desc')
            ->get();

        return response()->json([
            'success' => '1',
            'artist' => $artist,
            'albums' => $albums,
            'tracks' => $tracks
        ]);
    }

    public function bannerDetails($id)
    {
        $banner = \DB::table('tbl_banner')->where('bid', $id)->first();
        if (!$banner) return response()->json(['success' => '0', 'MSG' => 'Banner not found']);

        $songIds = explode(',', $banner->banner_post_id);
        $tracks = Audio::whereIn('id', $songIds)->where('audio_status', '1')->get();

        return response()->json([
            'success' => '1',
            'banner' => $banner,
            'tracks' => $tracks
        ]);
    }
    public function allSongs(Request $request)
    {
        $songs = Audio::where('audio_status', '1')->orderBy('id', 'desc')->paginate(20);
        return response()->json([
            'success' => '1',
            'data' => $songs
        ]);
    }

    public function allAlbums(Request $request)
    {
        $albums = Album::where('status', '1')->orderBy('aid', 'desc')->paginate(12);
        return response()->json([
            'success' => '1',
            'data' => $albums
        ]);
    }

    public function allArtists(Request $request)
    {
        $artists = Artist::orderBy('id', 'desc')->paginate(18);
        return response()->json([
            'success' => '1',
            'data' => $artists
        ]);
    }
}
