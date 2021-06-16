@extends('layouts.app')

@section('content')
<div class="container">
      <div class="row justify-content-center">
          <div class="col-md-8">
            <a href="/metarules" class="btn btn-primary">Back to meta-rules</a>

            <br><br><br>
              <h1><u>{{$metarule->name}}</u>  </h1>

              <br>
              <div class="form-group">
                 <label>Time Duration:</label>
                 <input class="form-control" disabled value="{{$metarule->time}}">
              </div>
              <div class="form-group">
                 <label>Action:</label>
                 <input class="form-control" disabled value="Set {{$metarule->action}} level">
              </div>
              <div class="form-group">
                 <label>Value:</label>
                 <input class="form-control" disabled value="{{$metarule->value}}">
              </div>

              <hr>
              <small>Created on {{$metarule->created_at}}  </small>
              <hr>

              <a href="/metarules/{{$metarule->id}}/edit" class="btn btn-warning">Edit</a>

              <div class="float-right">
                {!!Form::open(['action' => ['MetarulesController@destroy', $metarule->id], 'method' => 'POST', 'class' => 'pull-right'])!!}
                  {{Form::hidden('_method', 'DELETE')}}
                  {{Form::submit('Delete', ['class' => 'btn btn-danger'] )}}
                {!!Form::close()!!}
              </div>

    </div>
  </div>
</div>

@endsection
