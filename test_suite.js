import './podio_client.js';

function pass(test_name){
    console.log(`${test_name} PASS`);
}

function fail(test_name, reason){
    console.log(`${test_name} FAIL: ${reason}`);
}

pass("test");
