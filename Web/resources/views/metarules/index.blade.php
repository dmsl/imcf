
@extends('layouts.app')
  @section('content')
  <div class="container">
      <div class="row justify-content-center">
          <div class="col-md-8">
              <div class="card">
                  <div class="card-header">Meta-rules configuration</div>

                  <div class="card-body">
                      @if (session('status'))
                          <div class="alert alert-success" role="alert">
                              {{ session('status') }}
                          </div>
                      @endif

                      <!---You are logged in!--->

                      <div class="panel-body">
                        <div>
                          <div class="float-md-left"><h1>Meta-Rules Table</h1> </div>
                          <div class="float-md-right"><a href="/metarules/create" class="btn btn-primary">Create meta-rule</a> </div>
                        </div>

                        @if(count($metarules)>0)
                          <table class="table table-striped">
                              <tr>
                                <th>Description</th>
                                <th></th>
                                <th></th>
                              </tr>
                              @foreach($metarules as $metarule)
                              <tr>
                                <td><a href="/metarules/{{$metarule->id}}">{{$metarule->name}}</a></td>
                                <td><a href="/metarules/{{$metarule->id}}/edit" class="btn btn-warning">Edit</a></td>
                                <td>
                                  {!!Form::open(['action' => ['MetarulesController@destroy', $metarule->id], 'method' => 'POST', 'class' => 'pull-right'])!!}
                                    {{Form::hidden('_method', 'DELETE')}}
                                    {{Form::submit('Delete', ['class' => 'btn btn-danger'] )}}
                                  {!!Form::close()!!}
                                </td>
                              </tr>
                              @endforeach
                          </table>
                        @else
                          <br><br><br><br>
                          <p>There are no meta-rules configured.</p>
                        @endif

                      </div>
                  </div>
              </div>
          </div>
      </div>
  </div>


  @endsection
