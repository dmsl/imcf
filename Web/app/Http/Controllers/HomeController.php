<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Metarule;

class HomeController extends Controller
{
    /**
     * Create a new controller instance.
     *
     * @return void
     */
    public function __construct()
    {
        $this->middleware('auth');
    }

    /**
     * Show the application dashboard.
     *
     * @return \Illuminate\Contracts\Support\Renderable
     */
    public function index()
    {
        //return view('home');
        $metarules = Metarule::where('user_id', auth()->user()->id)->get();
        return view('metarules.index')->with('metarules',$metarules);
    }
}
