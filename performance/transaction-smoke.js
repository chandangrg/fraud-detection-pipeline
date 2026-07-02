import http from 'k6/http'; import {check,sleep} from 'k6';
export const options={vus:5,duration:'20s',thresholds:{http_req_failed:['rate<0.01'],http_req_duration:['p(95)<1000']}};
export default function(){const key=`k6-${__VU}-${__ITER}`;const r=http.post(`${__ENV.BASE_URL||'http://localhost:8080'}/api/v1/transactions`,JSON.stringify({idempotencyKey:key,accountId:'acct-low',amount:100,currency:'USD'}),{headers:{'Content-Type':'application/json'}});check(r,{'accepted':x=>x.status===201||x.status===200});sleep(0.2);}
