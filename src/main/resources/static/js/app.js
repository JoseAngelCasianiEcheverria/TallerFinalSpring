

const API = '/api';



const $  = (sel, raiz = document) => raiz.querySelector(sel);
const $$ = (sel, raiz = document) => Array.from(raiz.querySelectorAll(sel));

function numeroES(valor, decimales = 0) {
    if (valor === null || valor === undefined) return '—';
    return Number(valor).toLocaleString('es-CO', {
        minimumFractionDigits: decimales,
        maximumFractionDigits: decimales
    });
}

function fechaLarga(iso) {
    if (!iso) return '—';
    const d = new Date(iso);
    if (isNaN(d)) return iso;
    return d.toLocaleDateString('es-CO', { day: '2-digit', month: '2-digit', year: 'numeric' })
         + ' ' + d.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' });
}


async function api(ruta, opciones = {}) {
    let respuesta;
    try {
        respuesta = await fetch(API + ruta, {
            headers: { 'Content-Type': 'application/json' },
            ...opciones
        });
    } catch (e) {
        throw {
            status: 0,
            message: 'No se pudo conectar con la API. ¿Está corriendo la aplicación?',
            detalles: []
        };
    }

    if (respuesta.status === 204) return null;

    let cuerpo = null;
    try { cuerpo = await respuesta.json(); } catch (e) {  }

    if (!respuesta.ok) {
        throw {
            status: respuesta.status,
            message: (cuerpo && cuerpo.message)
                || `Error ${respuesta.status}: ${respuesta.statusText}`,
            detalles: (cuerpo && cuerpo.detalles) || [],
            path: cuerpo && cuerpo.path
        };
    }

    return cuerpo;
}



const aviso = $('#aviso');
const avisoTitulo = $('#aviso-titulo');
const avisoDetalles = $('#aviso-detalles');

function mostrarAviso(tipo, titulo, detalles = []) {
    aviso.className = 'aviso ' + tipo;
    avisoTitulo.textContent = titulo;
    avisoDetalles.innerHTML = '';

    detalles.forEach(d => {
        const li = document.createElement('li');
        li.textContent = d;
        avisoDetalles.appendChild(li);
    });

    aviso.classList.remove('oculto');
    aviso.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

const mostrarExito  = (t, d) => mostrarAviso('exito', t, d);
const mostrarError  = (e) => mostrarAviso('error',
    (e && e.message) ? e.message : 'Ocurrio un error inesperado', (e && e.detalles) || []);

const ocultarAviso = () => aviso.classList.add('oculto');




(function filtrarTelefono() {
    const campo = $('#telefono');
    if (!campo) return;

    const soloDigitos = texto => texto.replace(/\D+/g, '').slice(0, 10);

    campo.addEventListener('input', () => {
        const limpio = soloDigitos(campo.value);
        if (campo.value !== limpio) campo.value = limpio;
    });

    
    ['paste', 'drop'].forEach(evento => {
        campo.addEventListener(evento, () => {
            setTimeout(() => {
                const limpio = soloDigitos(campo.value);
                if (campo.value !== limpio) campo.value = limpio;
            }, 0);
        });
    });
})();




function mostrarPanel(nombre, escribirHash = true) {
    const boton = $('.pestana[data-panel="' + nombre + '"]');
    if (!boton) { nombre = 'inicio'; }

    $$('.pestana').forEach(b => b.classList.remove('activa'));
    $$('.panel').forEach(p => p.classList.remove('activo'));

    $('.pestana[data-panel="' + nombre + '"]').classList.add('activa');
    $('#panel-' + nombre).classList.add('activo');

    if (nombre === 'clientes') cargarClientes();
    if (nombre === 'vehiculos') cargarVehiculos();
    if (nombre === 'ventas')    cargarVentas();
    if (nombre === 'mantenimientos') cargarMantenimientos();
    if (nombre === 'inicio')    cargarResumen();

    if (escribirHash && location.hash !== '#' + nombre) {
        history.pushState(null, '', '#' + nombre);
    }
}

$$('.pestana').forEach(boton => {
    boton.addEventListener('click', () => mostrarPanel(boton.dataset.panel));
});

window.addEventListener('popstate', () => {
    mostrarPanel((location.hash || '#inicio').slice(1), false);
});


function crearAviso(idPrefijo) {
    const caja = $('#' + idPrefijo);
    const titulo = $('#' + idPrefijo + '-titulo');
    const detalles = $('#' + idPrefijo + '-detalles');

    return {
        ocultar: () => caja.classList.add('oculto'),
        exito: (t, d = []) => mostrarAvisoEn(caja, titulo, detalles, 'exito', t, d),
        error: (e) => mostrarAvisoEn(caja, titulo, detalles, 'error',
            (e && e.message) ? e.message : 'Ocurrio un error inesperado',
            (e && e.detalles) || [])
    };
}

function mostrarAvisoEn(caja, titulo, detalles, tipo, texto, lista) {
    caja.className = 'aviso ' + tipo;
    titulo.textContent = texto;
    detalles.innerHTML = '';
    lista.forEach(d => {
        const li = document.createElement('li');
        li.textContent = d;
        detalles.appendChild(li);
    });
    caja.classList.remove('oculto');
    caja.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}



async function cargarResumen() {
    
    await Promise.all($$('.tarjeta').map(async tarjeta => {
        const recurso = tarjeta.dataset.recurso;
        try {
            const datos = await api(recurso);
            tarjeta.classList.remove('tarjeta-error');
            $('[data-campo="conteo"]', tarjeta).textContent = numeroES(datos.length);
        } catch (e) {
            tarjeta.classList.add('tarjeta-error');
            $('[data-campo="conteo"]', tarjeta).textContent = e.status || 'error';
        }
    }));

    await cargarTasa();
}

async function cargarTasa() {
    try {
        const t = await api('/tasa-cambio');

        $('#tasa-valor').textContent = numeroES(t.tasa, 2);
        $('#tasa-unidad').textContent = `${t.monedaDestino} por ${t.monedaOrigen}`;
        $('#tasa-fecha').textContent = t.fechaActualizacion
            ? 'Publicada el ' + fechaLarga(t.fechaActualizacion)
            : 'Sin fecha de actualizacion';

        
        
        
        const insignia = $('#tasa-origen');
if (t.origen === 'api-externa') {
        insignia.textContent = 'Tasa de hoy';
        insignia.className = 'insignia externa';
    } else {
        insignia.textContent = 'Tasa de referencia';
        insignia.className = 'insignia respaldo';
    }
    } catch (e) {
        $('#tasa-valor').textContent = '—';
        $('#tasa-unidad').textContent = 'sin conexion';
        $('#tasa-fecha').textContent = e.message || '';
    }
}



const cuerpoClientes = $('#filas-clientes');
const form = $('#form-cliente');

let editandoId = null;

async function cargarClientes() {
    try {
        const clientes = await api('/clientes');

        cuerpoClientes.innerHTML = '';

        if (clientes.length === 0) {
            const tr = document.createElement('tr');
            tr.className = 'fila-vacia';
            const td = document.createElement('td');
            td.colSpan = 7;
            td.textContent = 'Todavia no hay clientes registrados.';
            tr.appendChild(td);
            cuerpoClientes.appendChild(tr);
            return;
        }

        clientes.forEach(c => cuerpoClientes.appendChild(filaCliente(c)));
    } catch (e) {
        cuerpoClientes.innerHTML = '';
        const tr = document.createElement('tr');
        tr.className = 'fila-vacia';
        const td = document.createElement('td');
        td.colSpan = 7;
        td.textContent = 'No se pudieron cargar los clientes: ' + e.message;
        tr.appendChild(td);
        cuerpoClientes.appendChild(tr);
    }
}

function celda(texto) {
    const td = document.createElement('td');
    td.textContent = (texto === null || texto === undefined || texto === '') ? '—' : texto;
    return td;
}

function filaCliente(c) {
    const tr = document.createElement('tr');

    tr.appendChild(celda(c.id));
    tr.appendChild(celda(c.nombre));
    tr.appendChild(celda(c.email));
    tr.appendChild(celda(c.telefono));
    tr.appendChild(celda(c.direccion));
    tr.appendChild(celda(fechaLarga(c.fechaRegistro)));

    const tdAcciones = document.createElement('td');
    tdAcciones.className = 'col-acciones';

    const btnEditar = document.createElement('button');
    btnEditar.type = 'button';
    btnEditar.className = 'boton mini';
    btnEditar.textContent = 'Editar';
    btnEditar.addEventListener('click', () => entrarEnEdicion(c));

    const btnBorrar = document.createElement('button');
    btnBorrar.type = 'button';
    btnBorrar.className = 'boton peligro';
    btnBorrar.textContent = 'Borrar';
    btnBorrar.style.marginLeft = '6px';
    btnBorrar.addEventListener('click', () => borrarCliente(c));

    tdAcciones.appendChild(btnEditar);
    tdAcciones.appendChild(btnBorrar);
    tr.appendChild(tdAcciones);

    return tr;
}

function entrarEnEdicion(c) {
    editandoId = c.id;

    $('#cliente-id').value = c.id;
    $('#nombre').value = c.nombre || '';
    $('#email').value = c.email || '';
    $('#telefono').value = c.telefono || '';
    $('#direccion').value = c.direccion || '';

    $('#form-titulo').textContent = 'Editar cliente #' + c.id;
    $('#btn-guardar').textContent = 'Actualizar';
    $('#btn-cancelar').classList.remove('oculto');

    $$('input', form).forEach(i => i.classList.remove('invalido'));
    $('#nombre').focus();
}

function salirDeEdicion() {
    editandoId = null;

    form.reset();
    $('#cliente-id').value = '';
    $('#form-titulo').textContent = 'Registrar cliente';
    $('#btn-guardar').textContent = 'Registrar';
    $('#btn-cancelar').classList.add('oculto');

    $$('input', form).forEach(i => i.classList.remove('invalido'));
}

form.addEventListener('submit', async evento => {
    evento.preventDefault();
    ocultarAviso();
    $$('input', form).forEach(i => i.classList.remove('invalido'));

    const datos = {
        nombre: $('#nombre').value.trim(),
        email: $('#email').value.trim(),
        telefono: $('#telefono').value.trim() || null,
        direccion: $('#direccion').value.trim() || null
    };

    try {
        if (editandoId === null) {
            await api('/clientes', { method: 'POST', body: JSON.stringify(datos) });
            mostrarExito('Cliente registrado.');
        } else {
            await api('/clientes/' + editandoId, { method: 'PUT', body: JSON.stringify(datos) });
            mostrarExito('Cliente #' + editandoId + ' actualizado.');
        }

        salirDeEdicion();
        await cargarClientes();
        await actualizarTarjetas();

    } catch (e) {
        mostrarError(e);
        marcarCamposInvalidos(e.detalles || []);
    }
});


function marcarCamposInvalidos(detalles, mapa) {
    marcarCamposInvalidosEn(detalles, mapa);
}

async function borrarCliente(c) {
    if (!confirm(`¿Borrar a ${c.nombre}?\n\nSi ya tiene ventas registradas, el sistema lo impedirá y te dirá por qué.`)) {
        return;
    }

    try {
        await api('/clientes/' + c.id, { method: 'DELETE' });
        mostrarExito('Cliente ' + c.nombre + ' eliminado.');
        await cargarClientes();
        await actualizarTarjetas();
    } catch (e) {
        
        
        
        mostrarError(e);
    }
}

$('#btn-cancelar').addEventListener('click', salirDeEdicion);
$('#btn-recargar').addEventListener('click', cargarClientes);


async function actualizarTarjetas() {
    const tarjeta = document.querySelector('.tarjeta[data-recurso="/clientes"]');
    if (!tarjeta) return;
    try {
        const datos = await api('/clientes');
        $('[data-campo="conteo"]', tarjeta).textContent = numeroES(datos.length);
    } catch (e) {  }
}


async function refrescarContadores() {
    await Promise.all($$('.tarjeta').map(async tarjeta => {
        try {
            const datos = await api(tarjeta.dataset.recurso);
            tarjeta.classList.remove('tarjeta-error');
            $('[data-campo="conteo"]', tarjeta).textContent = numeroES(datos.length);
        } catch (e) {
            tarjeta.classList.add('tarjeta-error');
            $('[data-campo="conteo"]', tarjeta).textContent = e.status || 'error';
        }
    }));
}



const avisoVehiculos = crearAviso('aviso-vehiculos');
const cuerpoVehiculos = $('#filas-vehiculos');
const formVehiculo = $('#form-vehiculo');

let editandoVehiculoId = null;
let filtroMarcaActivo = '';
let soloDisponiblesActivo = false;

async function cargarVehiculos() {
    const titulo = $('#titulo-catalogo');
    titulo.textContent = 'Cargando…';

    try {
        
        
        
        
        let datos;
        let descripcion = 'Todos los vehículos';

        if (filtroMarcaActivo) {
            datos = await api('/vehiculos/marca/' + encodeURIComponent(filtroMarcaActivo));
            descripcion = 'Vehículos de la marca "' + filtroMarcaActivo + '"';
        } else if (soloDisponiblesActivo) {
            datos = await api('/vehiculos/disponibles');
            descripcion = 'Solo vehículos disponibles';
        } else {
            datos = await api('/vehiculos');
        }

        if (soloDisponiblesActivo && filtroMarcaActivo) {
            datos = (await api('/vehiculos/marca/' + encodeURIComponent(filtroMarcaActivo)))
                .filter(v => v.estado === 'DISPONIBLE');
            descripcion = 'Disponibles de la marca "' + filtroMarcaActivo + '"';
        }

        titulo.textContent = descripcion + ' (' + datos.length + ')';

        cuerpoVehiculos.innerHTML = '';
        if (datos.length === 0) {
            cuerpoVehiculos.appendChild(filaVacia(9, 'Ningun vehículo coincide con el filtro.'));
            return;
        }
        datos.forEach(v => cuerpoVehiculos.appendChild(filaVehiculo(v)));

    } catch (e) {
        cuerpoVehiculos.innerHTML = '';
        cuerpoVehiculos.appendChild(filaVacia(9, 'No se pudieron cargar: ' + e.message));
    }
}

function filaVacia(colspan, texto) {
    const tr = document.createElement('tr');
    tr.className = 'fila-vacia';
    const td = document.createElement('td');
    td.colSpan = colspan;
    td.textContent = texto;
    tr.appendChild(td);
    return tr;
}


const PALABRA_ESTADO = {
    DISPONIBLE: 'Disponible',
    EN_MANTENIMIENTO: 'En servicio',
    VENDIDO: 'Vendido',
    EN_PROCESO: 'En proceso',
    FINALIZADO: 'Finalizado'
};

function badge(estado) {
    const span = document.createElement('span');
    span.className = 'badge ' + estado.toLowerCase();
    span.textContent = PALABRA_ESTADO[estado] || estado.replace(/_/g, ' ').toLowerCase();
    return span;
}

function filaVehiculo(v) {
    const tr = document.createElement('tr');

    tr.appendChild(celda(v.placa));
    tr.appendChild(celda(v.marca));
    tr.appendChild(celda(v.modelo));
    tr.appendChild(celda(v.anio));


    
    
    
    tr.appendChild(celda(v.color || '-'));

    const tdPrecio = celda('$ ' + numeroES(v.precio, 0));
    tdPrecio.className = 'num';
    tr.appendChild(tdPrecio);

    const tdEstado = document.createElement('td');
    tdEstado.appendChild(badge(v.estado));
    tr.appendChild(tdEstado);

    tr.appendChild(celdaCambiarEstado(v));

    const tdAcciones = document.createElement('td');
    tdAcciones.className = 'col-acciones';

    const btnEditar = document.createElement('button');
    btnEditar.type = 'button';
    btnEditar.className = 'boton mini';
    btnEditar.textContent = 'Editar';
    btnEditar.addEventListener('click', () => entrarEnEdicionVehiculo(v));

    const btnBorrar = document.createElement('button');
    btnBorrar.type = 'button';
    btnBorrar.className = 'boton peligro';
    btnBorrar.textContent = 'Borrar';
    btnBorrar.style.marginLeft = '6px';
    btnBorrar.addEventListener('click', () => borrarVehiculo(v));

    tdAcciones.appendChild(btnEditar);
    tdAcciones.appendChild(btnBorrar);
    tr.appendChild(tdAcciones);

    return tr;
}


function celdaCambiarEstado(v) {
    const td = document.createElement('td');
    td.className = 'celda-estado';

    const opciones = [];
    if (v.estado === 'DISPONIBLE') {
        opciones.push({ valor: 'vender', texto: 'Vender…' });
        opciones.push({ valor: 'mantenimiento', texto: 'Abrir servicio…' });
    } else if (v.estado === 'EN_MANTENIMIENTO') {
        opciones.push({ valor: 'cerrar-mantenimiento', texto: 'Cerrar servicio…' });
    }

    if (opciones.length === 0) {
        const txt = document.createElement('span');
        txt.className = 'estado-terminal';
        txt.textContent = 'sin transiciones';
        txt.title = 'Este vehiculo ya se vendio y no vuelve a estar disponible.';
        td.appendChild(txt);
        return td;
    }

    const sel = document.createElement('select');
    sel.setAttribute('aria-label', 'Cambiar el estado de ' + v.placa);

    const inicial = document.createElement('option');
    inicial.value = '';
    inicial.textContent = 'Cambiar estado…';
    sel.appendChild(inicial);

    opciones.forEach(o => {
        const opt = document.createElement('option');
        opt.value = o.valor;
        opt.textContent = o.texto;
        sel.appendChild(opt);
    });

    sel.addEventListener('change', () => ejecutarTransicion(v, sel.value, sel));
    td.appendChild(sel);
    return td;
}

function ejecutarTransicion(v, accion, select) {
    select.value = '';   

    if (accion === 'vender') {
        prepararVenta(v);
        return;
    }

    if (accion === 'mantenimiento') {
        abrirModalMantenimiento(v);
        return;
    }

    if (accion === 'cerrar-mantenimiento') {
        cerrarMantenimientoDe(v);
    }
}


function prepararVenta(vehiculo) {
    $$('.pestana').forEach(b => b.classList.remove('activa'));
    $$('.panel').forEach(p => p.classList.remove('activo'));
    $('.pestana[data-panel="ventas"]').classList.add('activa');
    $('#panel-ventas').classList.add('activo');

    cargarVentas().then(() => {
        selVehiculo.value = String(vehiculo.id);
        selVehiculo.dispatchEvent(new Event('change'));
        avisoVentas.exito('Vehículo ' + vehiculo.placa + ' elegido. Revisa el total y elige el cliente.');
        $('#venta-cliente').focus();
    });
}

async function cerrarMantenimientoDe(v) {
    try {
        const mantenimientos = await api('/mantenimientos/vehiculo/' + v.id);
        const abierto = mantenimientos.find(m => m.estado === 'EN_PROCESO');

        if (!abierto) {
            avisoVehiculos.error('No hay ningún servicio abierto en ' + v.placa + '.');
            return;
        }

        if (!confirm('¿Cerrar el servicio ' + abierto.tipo + ' de ' + v.placa + '?\n\nEl vehículo volverá a estar disponible.')) return;

        await api('/mantenimientos/' + abierto.id + '/cerrar', { method: 'PATCH' });

        avisoVehiculos.exito('Servicio cerrado. ' + v.placa + ' vuelve a estar disponible.');
        await cargarVehiculos();
        await refrescarContadores();

    } catch (e) {
        avisoVehiculos.error(e);
    }
}



let vehiculoDelModal = null;

function abrirModalMantenimiento(v) {
    vehiculoDelModal = v;
    $('#modal-subtitulo').textContent = v.placa + ' · ' + v.marca + ' ' + v.modelo;
    $('#m-tipo').value = '';
    $('#m-descripcion').value = '';
    $('#m-costo').value = '';
    $('#m-tipo').classList.remove('invalido');
    $('#m-costo').classList.remove('invalido');
    $('#modal-velo').classList.remove('oculto');
    $('#m-tipo').focus();
}

function cerrarModal() {
    $('#modal-velo').classList.add('oculto');
    vehiculoDelModal = null;
}

$('#modal-cerrar').addEventListener('click', cerrarModal);

$('#modal-velo').addEventListener('click', e => {
    if (e.target === $('#modal-velo')) cerrarModal();
});

document.addEventListener('keydown', e => {
    if (e.key === 'Escape' && !$('#modal-velo').classList.contains('oculto')) cerrarModal();
});

$('#modal-guardar').addEventListener('click', async () => {
    if (!vehiculoDelModal) return;

    avisoVehiculos.ocultar();
    $('#m-tipo').classList.remove('invalido');
    $('#m-costo').classList.remove('invalido');

    const costo = parseFloat($('#m-costo').value.replace(/\./g, '').replace(',', '.'));

    try {
        await api('/mantenimientos', {
            method: 'POST',
            body: JSON.stringify({
                vehiculoId: vehiculoDelModal.id,
                tipo: $('#m-tipo').value.trim(),
                descripcion: $('#m-descripcion').value.trim() || null,
                costo: isNaN(costo) ? $('#m-costo').value.trim() : costo
            })
        });

        const placa = vehiculoDelModal.placa;
        cerrarModal();
        avisoVehiculos.exito(placa + ' pasó a servicio y ya no está disponible para la venta.');
        await cargarVehiculos();
        await refrescarContadores();

    } catch (e) {
        avisoVehiculos.error(e);
        marcarCamposInvalidosEn(e.detalles || [], { tipo: 'm-tipo', costo: 'm-costo' });
        $('#modal-velo').classList.remove('oculto');
    }
});


function marcarCamposInvalidosEn(detalles, mapa) {
    detalles.forEach(d => {
        const sep = d.indexOf(':');
        if (sep < 0) return;
        const campo = d.slice(0, sep).trim();
        const id = mapa && mapa[campo] ? mapa[campo] : campo;
        const input = $('#' + id);
        if (input) input.classList.add('invalido');
    });
}

function entrarEnEdicionVehiculo(v) {
    editandoVehiculoId = v.id;

    $('#vehiculo-id').value = v.id;
    $('#v-placa').value = v.placa || '';
    $('#v-marca').value = v.marca || '';
    $('#v-modelo').value = v.modelo || '';
    $('#v-anio').value = v.anio == null ? '' : v.anio;
    $('#v-color').value = v.color || '';
    $('#v-precio').value = v.precio == null ? '' : v.precio;

    $('#form-vehiculo-titulo').textContent = 'Editar vehículo · ' + v.placa;
    $('#btn-guardar-vehiculo').textContent = 'Actualizar';
    $('#btn-cancelar-vehiculo').classList.remove('oculto');

    $$('input', formVehiculo).forEach(i => i.classList.remove('invalido'));
    $('#v-placa').focus();
}

function salirDeEdicionVehiculo() {
    editandoVehiculoId = null;
    formVehiculo.reset();
    $('#vehiculo-id').value = '';
    $('#form-vehiculo-titulo').textContent = 'Registrar vehículo';
    $('#btn-guardar-vehiculo').textContent = 'Registrar';
    $('#btn-cancelar-vehiculo').classList.add('oculto');
    $$('input', formVehiculo).forEach(i => i.classList.remove('invalido'));
}

formVehiculo.addEventListener('submit', async evento => {
    evento.preventDefault();
    avisoVehiculos.ocultar();
    $$('input', formVehiculo).forEach(i => i.classList.remove('invalido'));

    const anio = parseInt($('#v-anio').value, 10);
    const precio = parseFloat($('#v-precio').value.replace(/\./g, '').replace(',', '.'));

    const datos = {
        placa: $('#v-placa').value.trim(),
        marca: $('#v-marca').value.trim(),
        modelo: $('#v-modelo').value.trim(),
        anio: isNaN(anio) ? null : anio,
        color: $('#v-color').value.trim() || null,
        precio: isNaN(precio) ? $('#v-precio').value.trim() : precio
    };

    try {
        if (editandoVehiculoId === null) {
            await api('/vehiculos', { method: 'POST', body: JSON.stringify(datos) });
            avisoVehiculos.exito('Vehículo registrado. Queda disponible para la venta.');
        } else {
            await api('/vehiculos/' + editandoVehiculoId, { method: 'PUT', body: JSON.stringify(datos) });
            avisoVehiculos.exito('Vehículo #' + editandoVehiculoId + ' actualizado.');
        }
        salirDeEdicionVehiculo();
        await cargarVehiculos();
        await refrescarContadores();
    } catch (e) {
        avisoVehiculos.error(e);
        marcarCamposInvalidos(e.detalles || [], {
            placa: 'v-placa', marca: 'v-marca', modelo: 'v-modelo',
            anio: 'v-anio', color: 'v-color', precio: 'v-precio'
        });
    }
});

async function borrarVehiculo(v) {
    if (!confirm(`¿Borrar el vehículo con placa ${v.placa}?\n\nSi tiene ventas o servicios registrados, el sistema lo impedirá y te dirá por qué.`)) return;
    try {
        await api('/vehiculos/' + v.id, { method: 'DELETE' });
        avisoVehiculos.exito('Vehículo ' + v.placa + ' eliminado.');
        await cargarVehiculos();
        await refrescarContadores();
    } catch (e) {
        avisoVehiculos.error(e);
    }
}

$('#btn-cancelar-vehiculo').addEventListener('click', salirDeEdicionVehiculo);
$('#btn-recargar-vehiculos').addEventListener('click', cargarVehiculos);

$('#btn-filtrar').addEventListener('click', () => {
    filtroMarcaActivo = $('#filtro-marca').value.trim();
    soloDisponiblesActivo = $('#filtro-disponibles').checked;
    cargarVehiculos();
});

$('#btn-quitar-filtro').addEventListener('click', () => {
    filtroMarcaActivo = '';
    soloDisponiblesActivo = false;
    $('#filtro-marca').value = '';
    $('#filtro-disponibles').checked = false;
    cargarVehiculos();
});



const avisoVentas = crearAviso('aviso-ventas');
const cuerpoVentas = $('#filas-ventas');
const formVenta = $('#form-venta');

const selCliente = $('#venta-cliente');
const selVehiculo = $('#venta-vehiculo');
const btnGuardarVenta = $('#btn-guardar-venta');

async function cargarVentas() {
    try {
        const [ventas, clientes, disponibles] = await Promise.all([
            api('/ventas'),
            api('/clientes'),
            api('/vehiculos/disponibles')
        ]);

        llenarSelect(selCliente, clientes.map(c => ({
            valor: c.id,
            texto: c.nombre + ' — ' + c.email
        })), 'Selecciona un cliente');

        llenarSelect(selVehiculo, disponibles.map(v => ({
            valor: v.id,
            texto: `${v.placa} — ${v.marca} ${v.modelo} — $ ${numeroES(v.precio, 0)}`
        })), 'Selecciona un vehículo');

        cuerpoVentas.innerHTML = '';
        if (ventas.length === 0) {
            cuerpoVentas.appendChild(filaVacia(7, 'Todavía no hay ventas registradas.'));
            return;
        }
        ventas.forEach(v => cuerpoVentas.appendChild(filaVenta(v)));

    } catch (e) {
        cuerpoVentas.innerHTML = '';
        cuerpoVentas.appendChild(filaVacia(7, 'No se pudieron cargar las ventas: ' + e.message));
    }
}

function llenarSelect(select, opciones, placeholder) {
    select.innerHTML = '';
    const vacia = document.createElement('option');
    vacia.value = '';
    vacia.textContent = placeholder;
    select.appendChild(vacia);
    opciones.forEach(o => {
        const opt = document.createElement('option');
        opt.value = o.valor;
        opt.textContent = o.texto;
        select.appendChild(opt);
    });
}

function filaVenta(v) {
    const tr = document.createElement('tr');

    tr.appendChild(celda(fechaLarga(v.fechaVenta)));
    tr.appendChild(celda(v.clienteNombre));
    tr.appendChild(celda(v.placa + ' (' + v.marca + ')'));
    tr.appendChild(celda('$ ' + numeroES(v.valorVehiculo, 2)));

    const tdDesc = document.createElement('td');
    tdDesc.textContent = v.descuentoAplicado > 0 ? v.descuentoAplicado + ' %' : '—';
    if (v.descuentoAplicado > 0) {
        tdDesc.style.color = 'var(--exito)';
        tdDesc.style.fontWeight = '600';
    }
    tr.appendChild(tdDesc);

    tr.appendChild(celda('$ ' + numeroES(v.total, 2)));
    tr.appendChild(celda(v.precioUsd == null ? 'sin dato' : '$ ' + numeroES(v.precioUsd, 2)));

    return tr;
}


selVehiculo.addEventListener('change', async () => {
    const panel = $('#simulacion');
    btnGuardarVenta.disabled = true;

    if (!selVehiculo.value) {
        panel.classList.add('oculto');
        return;
    }

    try {
        const s = await api('/ventas/simular/' + selVehiculo.value);

        $('#sim-valor').textContent = '$ ' + numeroES(s.valorVehiculo, 2);
        $('#sim-descuento').textContent = s.descuentoAplicado > 0
            ? s.descuentoAplicado + ' %'
            : '0 %';
        $('#sim-total').textContent = '$ ' + numeroES(s.total, 2);



$('#sim-usd').textContent = s.precioUsd == null
        ? 'sin dato (no se pudo consultar la tasa del dia)'
        : '$ ' + numeroES(s.precioUsd, 2);

        $('#sim-explicacion').textContent = s.explicacionDescuento || '';

        panel.classList.remove('oculto');
        btnGuardarVenta.disabled = false;

    } catch (e) {
        panel.classList.add('oculto');
        avisoVentas.error(e);
    }
});

selCliente.addEventListener('change', actualizarBotonVenta);

function actualizarBotonVenta() {
    btnGuardarVenta.disabled = !(selCliente.value && selVehiculo.value);
}

formVenta.addEventListener('submit', async evento => {
    evento.preventDefault();
    avisoVentas.ocultar();

    try {
        await api('/ventas', {
            method: 'POST',
            body: JSON.stringify({
                clienteId: parseInt(selCliente.value, 10),
                vehiculoId: parseInt(selVehiculo.value, 10)
            })
        });

        const vendido = selVehiculo.options[selVehiculo.selectedIndex].text;
        avisoVentas.exito('Venta registrada. El vehículo ya no está disponible.');

        selVehiculo.value = '';
        $('#simulacion').classList.add('oculto');
        actualizarBotonVenta();

        await cargarVentas();
        await refrescarContadores();

    } catch (e) {
        
        
        avisoVentas.error(e);
        await cargarVentas();
    }
});

$('#btn-recargar-ventas').addEventListener('click', cargarVentas);




const avisoMantenimientos = crearAviso('aviso-mantenimientos');

async function cargarMantenimientos() {
    const cuerpo = $('#filas-mantenimientos');
    if (!cuerpo) return;

    const soloAbiertos = $('#filtro-solo-abiertos').checked;

    try {
        const todos = await api('/mantenimientos');
        const lista = soloAbiertos ? todos.filter(m => m.estado === 'EN_PROCESO') : todos;

        avisoMantenimientos.ocultar();
        cuerpo.innerHTML = '';

        if (lista.length === 0) {
            const tr = document.createElement('tr');
            tr.className = 'fila-vacia';
            const td = document.createElement('td');
            td.colSpan = 7;
            td.textContent = soloAbiertos
                ? 'No hay ningún servicio abierto en este momento.'
                : 'Todavía no hay servicios registrados.';
            tr.appendChild(td);
            cuerpo.appendChild(tr);
            return;
        }

        
        lista
            .slice()
            .sort((a, b) => String(b.fechaInicio).localeCompare(String(a.fechaInicio)))
            .forEach(m => cuerpo.appendChild(filaMantenimiento(m)));

    } catch (e) {
        cuerpo.innerHTML = '';
        avisoMantenimientos.error(e);
    }
}

function filaMantenimiento(m) {
    const tr = document.createElement('tr');

    tr.appendChild(celda(fechaLarga(m.fechaInicio)));
    tr.appendChild(celda(m.placa));

    const tdTipo = celda(m.tipo);
    tdTipo.className = 'celda-tipo';
    tr.appendChild(tdTipo);

    const tdDesc = celda(m.descripcion || '—');
    tdDesc.className = 'celda-desc';
    tr.appendChild(tdDesc);

    tr.appendChild(celda('$ ' + numeroES(m.costo, 2)));

    const tdEstado = document.createElement('td');
    tdEstado.appendChild(badge(m.estado));
    tr.appendChild(tdEstado);

    tr.appendChild(celdaAccionMantenimiento(m));

    return tr;
}

function celdaAccionMantenimiento(m) {
    const td = document.createElement('td');
    td.className = 'col-acciones';

    if (m.estado !== 'EN_PROCESO') {
        const txt = document.createElement('span');
        txt.className = 'estado-terminal';
        txt.textContent = m.fechaFin ? 'cerrado ' + fechaLarga(m.fechaFin) : 'cerrado';
        td.appendChild(txt);
        return td;
    }

    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'boton mini';
    btn.textContent = 'Cerrar';
    btn.addEventListener('click', () => cerrarMantenimiento(m));
    td.appendChild(btn);

    return td;
}

async function cerrarMantenimiento(m) {
    const confirmar = confirm(
        '¿Cerrar el servicio "' + m.tipo + '" de ' + m.placa + '?\n\n'
        + 'El vehículo vuelve a estar disponible si no le queda ningún servicio abierto.');
    if (!confirmar) return;

    try {
        await api('/mantenimientos/' + m.id + '/cerrar', { method: 'PATCH' });
        avisoMantenimientos.exito('Servicio cerrado. ' + m.placa + ' vuelve a estar disponible.');
        await cargarMantenimientos();
        await refrescarContadores();
    } catch (e) {
        avisoMantenimientos.error(e);
    }
}

$('#btn-recargar-mantenimientos').addEventListener('click', cargarMantenimientos);
$('#filtro-solo-abiertos').addEventListener('change', cargarMantenimientos);


document.addEventListener('DOMContentLoaded', () => {
    // El build sale del body, no de aqui a proposito: si el navegador tiene un
    // app.js viejo cacheado, el body nuevo mostraria la version nueva y el
    // script viejo haria todo lo demas. Con el numero en el body, el .js viejo
    // no puede "arreglar" lo que el HTML nuevo afirma.
    const marca = document.body.dataset.build || 'sin version';
    const destino = document.getElementById('build');
    if (destino) destino.textContent = marca;

    cargarResumen();

    const inicial = (location.hash || '').slice(1);
    if (inicial) mostrarPanel(inicial, false);
});
