<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Metarule;
use App\Result;
use App\User;
use DB;

class MetarulesController extends Controller
{
  /** REMEMBER TO ADD to prevent unauthorised users to create/view/edit data
     * Create a new controller instance.
     *
     * @return void
     */
    /*public function __construct()
    {
        $this->middleware('auth');
    }*/

    public function testapi()
    {
       //$metarules = Metarule::get();
       //return $metarules;

      $metarule = new Metarule;
      $metarule->name="testName";
      $metarule->time="11:11-11:11";
      $metarule->action="LIGHT";
      $metarule->value="1111";
      $metarule->user_id=1;
      $metarule->save();
    }

    public function get_metarules()
    {
       $metarules = Metarule::get();
       return $metarules;
    }

     public function results()
    {      
      $results = Result::orderBy('id','DESC')->get();
      $kWhLimit = Metarule::where('name', 'kWh Preferred Limit')->get();

      $totalNumRules = DB::select('select count(id) as allRulesCounter from metarules');
      $users = DB::select('select user_id, users.name, count(*) as rulesCounter from metarules join users on users.id=metarules.user_id group by user_id,users.name');


      return view('results.index')->with('results',$results)->with('kWhLimit',$kWhLimit)->with('users',$users)->with('totalNumRules',$totalNumRules);
    }

    public function update_results(Request $request)
    {
       //$results = Result::get();
       //return $results;

      /*$results = new Result;
      $results->ep_kwH="9014";
      $results->ep_totalError="3.1784";
      $results->ep_convenienceError="3.1784";
      $results->ep_budgetError="0";
      $results->ep_standardDeviation="3.5981";
      $results->save();*/

      $results = new Result;
      $results->ep_kwH=$request->input('ep_kWh');
      $results->ep_totalError=$request->input('ep_totalError');
      $results->ep_convenienceError=$request->input('ep_convenienceError');
      $results->ep_budgetError=$request->input('ep_budgetError');
      $results->ep_standardDeviation=$request->input('ep_standardDeviation');
      $results->save();

      return 'success';
    }



    /**
     * Display a listing of the resource.
     *
     * @return \Illuminate\Http\Response
     */
    public function index()
    {
      $metarules = Metarule::get();
      //$metarules = Metarule::where('user_id', auth()->user()->id)->get();
      //$metarules= Metarule::orderBy('created_at','desc')->paginate(10);
      return view('metarules.index')->with('metarules',$metarules);
    }


    /**
     * Show the form for creating a new resource.
     *
     * @return \Illuminate\Http\Response
     */
    public function create()
    {
        return view('metarules.create');
    }

    /**
     * Store a newly created resource in storage.
     *
     * @param  \Illuminate\Http\Request  $request
     * @return \Illuminate\Http\Response
     */
    public function store(Request $request)
    {
      $this->validate($request, [
      'name' => ['required', 'string', 'max:255'],
      'action' => ['required', 'string', 'max:50'],
      'value' => ['required', 'string', 'max:20'],
      ]);

      if ($request->input('action')=='kWh')
      {
        $tempTime='kWh';

        $kWhExist = Metarule::where('name', 'kWh Preferred Limit')->get();

        if (count($kWhExist)>0)
          return redirect('/metarules')->with('warning', 'There is already a registered constraint Meta-rule for KwH if you wish to edit it.');
        
      }
      else
      {
        $this->validate($request, [
        'name' => ['required', 'string', 'max:255'],
        'timeFrom' => ['required', 'string', 'max:50'],
        'timeTo' => ['required', 'string', 'max:50'],
        'action' => ['required', 'string', 'max:50'],
        'value' => ['required', 'string', 'max:20'],
        ]);

        $tempTime=$request->input('timeFrom') . '-' . $request->input('timeTo');
      }


    //create Site
    $metarule = new Metarule;
    $metarule->name=$request->input('name');
    $metarule->time=$tempTime;
    $metarule->action=$request->input('action');
    $metarule->value=$request->input('value');
    $metarule->user_id=auth()->user()->id;
    $metarule->save();

    return redirect('/metarules')->with('success', 'Meta-rule created!');
    }

    /**
     * Display the specified resource.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function show($id)
    {
      $metarule= Metarule::find($id);
      return view('metarules.show')->with('metarule',$metarule);
    }

    /**
     * Show the form for editing the specified resource.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function edit($id)
    {
      $metarule= Metarule::find($id);
      return view('metarules.edit')->with('metarule',$metarule);
    }

    /**
     * Update the specified resource in storage.
     *
     * @param  \Illuminate\Http\Request  $request
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function update(Request $request, $id)
    {
      $this->validate($request, [
      'name' => ['required', 'string', 'max:255'],
      'action' => ['required', 'string', 'max:50'],
      'value' => ['required', 'string', 'max:20'],
      ]);

      if ($request->input('action')=='kWh')
      {
        $tempTime='kWh';
      }
      else
      {
        $this->validate($request, [
        'name' => ['required', 'string', 'max:255'],
        'timeFrom' => ['required', 'string', 'max:50'],
        'timeTo' => ['required', 'string', 'max:50'],
        'action' => ['required', 'string', 'max:50'],
        'value' => ['required', 'string', 'max:20'],
        ]);

        $tempTime=$request->input('timeFrom') . '-' . $request->input('timeTo');
      }

      //update POI and Site
      $metarule= Metarule::find($id);
      $metarule->name=$request->input('name');
      $metarule->time=$tempTime;
      $metarule->action=$request->input('action');
      $metarule->value=$request->input('value');
      $metarule->save();

      return redirect('/metarules')->with('success', 'Meta-rule edited!');

    }

    /**
     * Remove the specified resource from storage.
     *
     * @param  int  $id
     * @return \Illuminate\Http\Response
     */
    public function destroy($id)
    {
      $metarule= Metarule::find($id);
      $metarule->delete();
      return redirect('/metarules')->with('success', 'Meta-rule Removed!');
    }
}
