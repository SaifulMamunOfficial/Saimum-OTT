<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class StudentHubController extends Controller
{
    public function dashboard(Request $request)
    {
        $user = $request->user();

        // Check if user is a student
        if ($user->role !== 'student') {
            return response()->json([
                'success' => '0',
                'MSG' => 'Access denied. Only students can access this area.'
            ], 403);
        }

        return response()->json([
            'success' => '1',
            'MSG' => 'Welcome to Student Hub',
            'data' => [
                'student_id' => $user->student_id,
                'academic_records' => [], // Placeholder for future data
                'notice_board' => [
                    ['title' => 'Welcome!', 'date' => now()->toDateString()]
                ]
            ]
        ]);
    }
}
